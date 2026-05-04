package com.DigitalDining.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DigitalDining.demo.model.WeeklyMenuCategory;
import com.DigitalDining.demo.model.WeeklyMenuDay;
import com.DigitalDining.demo.model.WeeklyMenuDayView;
import com.DigitalDining.demo.model.WeeklyMenuItem;
import com.DigitalDining.demo.model.WeeklyMenuItemView;
import com.DigitalDining.demo.model.WeeklyMenuOrderSummary;
import com.DigitalDining.demo.repository.WeeklyMenuDayRepository;

@Service
public class WeeklyMenuService {

	private final WeeklyMenuDayRepository dayRepository;
    private final ZoneId zoneId = ZoneId.of("Europe/Budapest");

    public WeeklyMenuService(WeeklyMenuDayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }

    @Transactional
    public List<WeeklyMenuDayView> getNextSevenDays() {
        LocalDate start = LocalDate.now(zoneId);
        LocalDate end = start.plusDays(6);

        ensureDaysExist(start, end);

        return dayRepository.findByMenuDateBetweenOrderByMenuDateAsc(start, end)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public WeeklyMenuDayView getDayView(LocalDate date) {
        ensureDaysExist(date, date);
        WeeklyMenuDay day = dayRepository.findByMenuDate(date)
                .orElseThrow(() -> new IllegalArgumentException("A napi menü nem található"));
        return toView(day);
    }
    
    @Transactional
    public WeeklyMenuOrderSummary buildOrderSummary(LocalDate date, String soupOption, String mainOption, String dessertOption) {
        WeeklyMenuDay day = dayRepository.findByMenuDate(date)
                .orElseThrow(() -> new IllegalArgumentException("A napi menü nem található"));

        List<WeeklyMenuItem> items = day.getItems();

        WeeklyMenuItem soup = findByCategoryAndOption(items, WeeklyMenuCategory.SOUP, soupOption);
        WeeklyMenuItem main = findByCategoryAndOption(items, WeeklyMenuCategory.MAIN, mainOption);
        WeeklyMenuItem dessert = findByCategoryAndOption(items, WeeklyMenuCategory.DESSERT, dessertOption);

        List<WeeklyMenuItem> chosen = new ArrayList<>();
        if (soup != null) chosen.add(soup);
        if (main != null) chosen.add(main);
        if (dessert != null) chosen.add(dessert);

        if (chosen.isEmpty()) {
            throw new IllegalArgumentException("Legalább egy opciót válassz a kosárhoz adáshoz.");
        }

        BigDecimal total = chosen.stream()
                .map(WeeklyMenuItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String descriptionHtml = chosen.stream()
                .map(i -> "<strong>" + i.getCategory().getDisplayName() + " " + i.getOptionLabel() + ":</strong> " + i.getName())
                .collect(java.util.stream.Collectors.joining("<br>"));

        String cartLabel = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) + " (" + weekdayHu(date) + ") menü";

        return new WeeklyMenuOrderSummary(
                date,
                dayTitle(date),
                cartLabel,
                descriptionHtml,
                total
        );
    }

    private WeeklyMenuItem findByCategoryAndOption(List<WeeklyMenuItem> items, WeeklyMenuCategory category, String option) {
        if (option == null || option.isBlank()) {
            return null;
        }
        return items.stream()
                .filter(i -> i.getCategory() == category && option.equalsIgnoreCase(i.getOptionLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Érvénytelen választás: " + category.getDisplayName() + " / " + option));
    }

    private void ensureDaysExist(LocalDate start, LocalDate end) {
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (dayRepository.findByMenuDate(current).isEmpty()) {
                dayRepository.save(createSampleDay(current));
            }
            current = current.plusDays(1);
        }
    }

    private WeeklyMenuDay createSampleDay(LocalDate date) {
        WeeklyMenuDay day = new WeeklyMenuDay();
        day.setMenuDate(date);
        day.setTitle(dayTitle(date));
        day.setSubtitle("Frissen készítve, napi ajánlatként összeállítva");

        int seed = date.getDayOfYear();

        // Levesek
        day.addItem(createItem(day, WeeklyMenuCategory.SOUP, "A",
                soupNameA(seed), "Házi készítésű, friss alapanyagokkal", price(990), 1));
        day.addItem(createItem(day, WeeklyMenuCategory.SOUP, "B",
                soupNameB(seed), "Könnyű, szezonális választás", price(1090), 2));

        // Főételek
        day.addItem(createItem(day, WeeklyMenuCategory.MAIN, "A",
                mainNameA(seed), "Teljes értékű napi menü", price(2490), 3));
        day.addItem(createItem(day, WeeklyMenuCategory.MAIN, "B",
                mainNameB(seed), "Kiemelt ajánlat a napra", price(2790), 4));
        day.addItem(createItem(day, WeeklyMenuCategory.MAIN, "C",
                mainNameC(seed), "Bőséges, éttermi tálalás", price(2990), 5));

        // Desszertek
        day.addItem(createItem(day, WeeklyMenuCategory.DESSERT, "A",
                dessertNameA(seed), "Édes lezárás a menühöz", price(790), 6));
        day.addItem(createItem(day, WeeklyMenuCategory.DESSERT, "B",
                dessertNameB(seed), "Könnyű desszert opció", price(890), 7));

        return day;
    }

    private WeeklyMenuItem createItem(WeeklyMenuDay day,
                                      WeeklyMenuCategory category,
                                      String optionLabel,
                                      String name,
                                      String description,
                                      BigDecimal price,
                                      int displayOrder) {
        WeeklyMenuItem item = new WeeklyMenuItem();
        item.setDay(day);
        item.setCategory(category);
        item.setOptionLabel(optionLabel);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setDisplayOrder(displayOrder);
        return item;
    }

    private WeeklyMenuDayView toView(WeeklyMenuDay day) {
        Map<WeeklyMenuCategory, List<WeeklyMenuItem>> grouped = day.getItems().stream()
                .collect(Collectors.groupingBy(
                        WeeklyMenuItem::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MMMM d., EEEE", new Locale("hu", "HU"));

        return new WeeklyMenuDayView(
                day.getMenuDate(),
                weekdayHu(day.getMenuDate()),
                day.getMenuDate().format(formatter),
                day.getMenuDate().equals(LocalDate.now(zoneId)),
                day.getTitle(),
                day.getSubtitle(),
                mapItems(grouped.getOrDefault(WeeklyMenuCategory.SOUP, List.of())),
                mapItems(grouped.getOrDefault(WeeklyMenuCategory.MAIN, List.of())),
                mapItems(grouped.getOrDefault(WeeklyMenuCategory.DESSERT, List.of()))
        );
    }

    private List<WeeklyMenuItemView> mapItems(List<WeeklyMenuItem> items) {
        return items.stream()
                .sorted(Comparator.comparingInt(i -> i.getDisplayOrder() != null ? i.getDisplayOrder() : 999))
                .map(i -> new WeeklyMenuItemView(
                        i.getOptionLabel(),
                        i.getName(),
                        i.getDescription(),
                        i.getPrice()
                ))
                .toList();
    }

    private String weekdayHu(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Hétfői";
            case TUESDAY -> "Keddi";
            case WEDNESDAY -> "Szerdai";
            case THURSDAY -> "Csütörtöki";
            case FRIDAY -> "Pénteki";
            case SATURDAY -> "Szombati";
            case SUNDAY -> "Vasárnapi";
        };
    }

    private String dayTitle(LocalDate date) {
        return weekdayHu(date) + " menü";
    }

    private BigDecimal price(int amount) {
        return BigDecimal.valueOf(amount);
    }

    private String soupNameA(int seed) {
        return List.of(
                "Tyúkhúsleves cérnametélttel",
                "Gulyásleves",
                "Sütőtökkrémleves",
                "Tárkonyos zöldségleves",
                "Paradicsomleves bazsalikommal",
                "Hagymakrémleves"
        ).get(seed % 6);
    }

    private String soupNameB(int seed) {
        return List.of(
                "Csirkeraguleves",
                "Karfiolkrémleves",
                "Lencseleves",
                "Minestrone",
                "Brokkolikrémleves",
                "Bableves füstölt ízzel"
        ).get((seed + 2) % 6);
    }

    private String mainNameA(int seed) {
        return List.of(
                "Rántott csirkemell burgonyapürével",
                "Sertéspörkölt nokedlivel",
                "Grillezett csirkemell jázminrizzsel",
                "Paprikás csirke galuskával",
                "Bolognai spagetti",
                "Zöldfűszeres pulykamell"
        ).get(seed % 6);
    }

    private String mainNameB(int seed) {
        return List.of(
                "Marhapörkölt tarhonyával",
                "Roston sült lazac salátával",
                "Sajttal töltött csirkemell",
                "Csirkés penne tejszínes szósszal",
                "Fasírt petrezselymes burgonyával",
                "Rántott szelet vegyes körettel"
        ).get((seed + 1) % 6);
    }

    private String mainNameC(int seed) {
        return List.of(
                "Vegetáriánus rakott zöldség",
                "Sült kacsacomb párolt káposztával",
                "BBQ csirkeszárny menü",
                "Szezámos sertéscsíkok wok zöldséggel",
                "Lasagne házias raguval",
                "Mediterrán csirkemell grillezett zöldségekkel"
        ).get((seed + 3) % 6);
    }

    private String dessertNameA(int seed) {
        return List.of(
                "Csokoládés brownie",
                "Madártej",
                "Kókuszos piskóta",
                "Almás pite",
                "Túrógombóc vaníliaöntettel",
                "Epres pohárkrém"
        ).get(seed % 6);
    }

    private String dessertNameB(int seed) {
        return List.of(
                "Somlói galuska",
                "Panna cotta erdei gyümölccsel",
                "Citrusos túrótorta",
                "Vaníliás puding gyümölccsel",
                "Mákos guba",
                "Morzsasüti vaníliafagyival"
        ).get((seed + 2) % 6);
    }
}
