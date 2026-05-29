package com.proyecto.version1.Features.Reportes_Prenomina.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;

public final class ColombiaFestivosUtils {

    private ColombiaFestivosUtils() {
    }

    public static boolean esDominicalOFestivo(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SUNDAY || festivosColombia(fecha.getYear()).contains(fecha);
    }

    public static Set<LocalDate> festivosColombia(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // Festivos fijos nacionales
        holidays.add(LocalDate.of(year, 1, 1));
        holidays.add(LocalDate.of(year, 5, 1));
        holidays.add(LocalDate.of(year, 7, 20));
        holidays.add(LocalDate.of(year, 8, 7));
        holidays.add(LocalDate.of(year, 12, 8));
        holidays.add(LocalDate.of(year, 12, 25));

        // Festivos de ley Emiliani (se trasladan al lunes siguiente)
        holidays.add(siguienteLunes(LocalDate.of(year, 1, 6)));
        holidays.add(siguienteLunes(LocalDate.of(year, 3, 19)));
        holidays.add(siguienteLunes(LocalDate.of(year, 6, 29)));
        holidays.add(siguienteLunes(LocalDate.of(year, 8, 15)));
        holidays.add(siguienteLunes(LocalDate.of(year, 10, 12)));
        holidays.add(siguienteLunes(LocalDate.of(year, 11, 1)));
        holidays.add(siguienteLunes(LocalDate.of(year, 11, 11)));

        // Festivos religiosos móviles
        LocalDate easter = calcularPascua(year);
        holidays.add(siguienteLunes(easter.minusDays(3))); // Jueves Santo
        holidays.add(siguienteLunes(easter.minusDays(2))); // Viernes Santo
        holidays.add(siguienteLunes(easter.plusDays(43))); // Ascensión
        holidays.add(siguienteLunes(easter.plusDays(64))); // Corpus Christi
        holidays.add(siguienteLunes(easter.plusDays(71))); // Sagrado Corazón

        return holidays;
    }

    private static LocalDate siguienteLunes(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    // Algoritmo gregoriano para calcular Pascua
    private static LocalDate calcularPascua(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}

