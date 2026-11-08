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
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // ↓ 追加:合材予約の月別まとめ(引取済みだけ)
    @GetMapping("/monthly")
    public String monthly(@RequestParam(name = "ym", required = false) String ym,
                          Model model) {
        YearMonth target = parseYearMonth(ym);

        List<ReservationItemRepository.DailyItemTotal> rows =
                reservationItemRepository.sumDailyByItem(
                        target.atDay(1), target.atEndOfMonth(), ReservationStatus.PICKED_UP);

        // 行:1日〜末日
        List<LocalDate> days = new ArrayList<>();
        for (int d = 1; d <= target.lengthOfMonth(); d++) {
            days.add(target.atDay(d));
        }

        // 列:この月に引取のあった品目
        Set<Long> itemIds = new HashSet<>();
        for (ReservationItemRepository.DailyItemTotal r : rows) {
            if (r.getItemId() != null) {
                itemIds.add(r.getItemId());
            }
        }

        ItemTonSummary summary = new ItemTonSummary(days, findItemsSorted(itemIds));
        for (ReservationItemRepository.DailyItemTotal r : rows) {
            summary.add(r.getPickupDate(), r.getItemId(), r.getTotal());
        }

        model.addAttribute("ym", target);
        model.addAttribute("summary", summary);
        return "reservations/monthly";
    }

    // ↓ 追加:合材予約の年別まとめ(引取済みだけ)
    @GetMapping("/yearly")
    public String yearly(@RequestParam(name = "year", required = false) Integer year,
                         Model model) {
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;

        List<ReservationItemRepository.MonthlyItemTotal> rows =
                reservationItemRepository.sumMonthlyByItem(
                        LocalDate.of(targetYear, 1, 1), LocalDate.of(targetYear, 12, 31),
                        ReservationStatus.PICKED_UP);

        // 行:1月〜12月
        List<Integer> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(m);
        }

        // 列:この年に引取のあった品目
        Set<Long> itemIds = new HashSet<>();
        for (ReservationItemRepository.MonthlyItemTotal r : rows) {
            if (r.getItemId() != null) {
                itemIds.add(r.getItemId());
            }
        }

        ItemTonSummary summary = new ItemTonSummary(months, findItemsSorted(itemIds));
        for (ReservationItemRepository.MonthlyItemTotal r : rows) {
            summary.add(r.getMonthNo(), r.getItemId(), r.getTotal());
        }

        model.addAttribute("year", targetYear);
        model.addAttribute("summary", summary);
        return "reservations/yearly";
    }

    // 品目を、名前の順に並べて取得する
    private List<Item> findItemsSorted(Set<Long> itemIds) {
        List<Item> list = new ArrayList<>(itemRepository.findAllById(itemIds));
        list.sort(Comparator.comparing(Item::getName));
        return list;
    }

    // 「2026-11」の形の文字を年月にする(空や変な形なら今月)
    private YearMonth parseYearMonth(String ym) {
        if (ym == null || ym.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(ym);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }

    // 予約入力画面
    @GetMapping("/new")
    public String newForm(Model model) {
        ReservationForm form = new ReservationForm();
        form.setPickupDate(LocalDate.now());
        form.setShift(Shift.DAY);
        form.getLines().add(new ReservationLine());
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
        reservation.setStatus(ReservationStatus.RESERVED);

        for (ReservationLine line : reservationForm.getLines()) {
            ReservationItem ri = new ReservationItem();
            ri.setItem(line.getItem());
            ri.setQuantity(line.getQuantity());
            reservation.addItem(ri);
        }

        reservationRepository.save(reservation);

        redirectAttributes.addFlashAttribute("message",
                "「" + reservation.getCustomerName() + "」の予約を登録しました");
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
        redirectAttributes.addAttribute("date", reservation.getPickupDate().toString());
        return "redirect:/reservations";
    }
}