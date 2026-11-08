package com.example.dispatch;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final ItemRepository itemRepository;

    public ReservationController(ReservationRepository reservationRepository,
                                 ReservationItemRepository reservationItemRepository,
                                 ItemRepository itemRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
        this.itemRepository = itemRepository;
    }

    // 画面で使う選択肢
    @ModelAttribute("items")
    public List<Item> items() {
        return itemRepository.findAllByOrderByNameAsc();
    }

    @ModelAttribute("shifts")
    public Shift[] shifts() {
        return Shift.values();
    }

    // 予約一覧(日付の指定がなければ今日)
    @GetMapping
    public String list(@RequestParam(name = "date", required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       Model model) {
        if (date == null) {
            date = LocalDate.now();
        }
        model.addAttribute("date", date);
        model.addAttribute("reservations",
                reservationRepository.findByPickupDateOrderByShiftAscIdAsc(date));
        model.addAttribute("totals",
                reservationItemRepository.sumByItem(date, ReservationStatus.CANCELLED));
        return "reservations/list";
    }

    // 予約入力画面
    @GetMapping("/new")
    public String newForm(Model model) {
        ReservationForm form = new ReservationForm();
        form.setPickupDate(LocalDate.now());         // 最初は今日
        form.setShift(Shift.DAY);                    // 最初は昼
        form.getLines().add(new ReservationLine());  // 品目の行を1行用意
        model.addAttribute("reservationForm", form);
        return "reservations/form";
    }

    // 予約の登録
    @PostMapping
    public String create(@Valid @ModelAttribute ReservationForm reservationForm,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reservations/form";
        }

        Reservation reservation = new Reservation();
        reservation.setPickupDate(reservationForm.getPickupDate());
        reservation.setShift(reservationForm.getShift());
        reservation.setCustomerName(reservationForm.getCustomerName());
        reservation.setRemarks(reservationForm.getRemarks());
        reservation.setStatus(ReservationStatus.RESERVED);  // 最初は「予約中」

        for (ReservationLine line : reservationForm.getLines()) {
            ReservationItem ri = new ReservationItem();
            ri.setItem(line.getItem());
            ri.setQuantity(line.getQuantity());
            reservation.addItem(ri);
        }

        reservationRepository.save(reservation);  // 品目の行も一緒に保存される

        redirectAttributes.addFlashAttribute("message",
                "「" + reservation.getCustomerName() + "」の予約を登録しました");
        // ↓ 直したところ:toString() で「2026-11-07」の形にしてURLに渡す
        redirectAttributes.addAttribute("date", reservation.getPickupDate().toString());
        return "redirect:/reservations";
    }

    // 「引取済みにする」
    @PostMapping("/{id}/pickup")
    public String pickup(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return changeStatus(id, ReservationStatus.PICKED_UP, redirectAttributes);
    }

    // 「キャンセル」
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return changeStatus(id, ReservationStatus.CANCELLED, redirectAttributes);
    }

    // 状態を変える共通の処理
    private String changeStatus(Long id, ReservationStatus status,
                                RedirectAttributes redirectAttributes) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setStatus(status);
        reservationRepository.save(reservation);

        redirectAttributes.addFlashAttribute("message",
                "「" + reservation.getCustomerName() + "」を" + status.getLabel() + "にしました");
        // ↓ 直したところ
        redirectAttributes.addAttribute("date", reservation.getPickupDate().toString());
        return "redirect:/reservations";
    }
}