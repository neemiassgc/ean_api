package com.api;

import com.api.entity.Price;
import com.api.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public final class Resources {

    public static final List<Product> PRODUCTS_SAMPLE = List.of(
        Product.builder()
            .description("ACHOC PO NESCAU 800G")
            .sequenceCode(29250)
            .barcode("7891000055120")
            .build(),
        Product.builder()
            .description("AMENDOIM SALG CROKISSIMO 400G PIMENTA")
            .sequenceCode(120983)
            .barcode("7896336010058")
            .build(),
        Product.builder()
            .description("BALA GELATINA FINI 500G BURGUER")
            .barcode("78982797922990")
            .sequenceCode(93556)
            .build(),
        Product.builder()
            .description("BISC ROSQ MARILAN 350G INT")
            .barcode("7896003737257")
            .sequenceCode(127635)
            .build(),
        Product.builder()
            .description("BISC WAFER TODDY 132G CHOC")
            .barcode("7896071024709")
            .sequenceCode(122504)
            .build(),
        Product.builder()
            .description("BISC ZABET 350G LEITE")
            .barcode("7896085087028")
            .sequenceCode(144038)
            .build(),
        Product.builder()
            .description("BOLINHO BAUDUC 40G GOTAS CHOC")
            .barcode("7891962037219")
            .sequenceCode(98894)
            .build(),
        Product.builder()
            .description("CAFE UTAM 500G")
            .sequenceCode(2909)
            .barcode("7896656800018")
            .build(),
        Product.builder()
            .description("LEITE PO NINHO 400G INTEG")
            .barcode("7891000000427")
            .sequenceCode(892)
            .build(),
        Product.builder()
            .description("LIMP M.USO OMO 500ML DESINF HERBAL")
            .barcode("7891150080850")
            .sequenceCode(141947)
            .build(),
        Product.builder()
            .description("MAIONESE QUERO 210G TP")
            .barcode("7896102513714")
            .sequenceCode(87689)
            .build(),
        Product.builder()
            .description("MILHO VDE PREDILECTA 170G LT")
            .barcode("7896292340503")
            .sequenceCode(134049)
            .build(),
        Product.builder()
            .description("OLEO MILHO LIZA 900ML")
            .barcode("7896036090619")
            .sequenceCode(5648)
            .build(),
        Product.builder()
            .description("PAP ALUMINIO WYDA 30X7.5")
            .barcode("7898930672441")
            .sequenceCode(30881)
            .build(),
        Product.builder()
            .description("PAP HIG F.D NEVE C8 COMPACTO NEUT")
            .barcode("7891172422379")
            .sequenceCode(25336)
            .build(),
        Product.builder()
            .description("REFRIG ANTARCT 600ML PET GUARANA")
            .barcode("7891991002646")
            .sequenceCode(6367)
            .build(),
        Product.builder()
            .description("SAL MARINHO LEBRE 500G GOURMET")
            .barcode("7896110195162")
            .sequenceCode(128177)
            .build(),
        Product.builder()
            .description("VINAGRE CASTELO 500ML VD FRUTA MACA")
            .barcode("7896048285539")
            .sequenceCode(125017)
            .build()
    );

        static {
            PRODUCTS_SAMPLE.get(0)
                .addPrice(new Price(new BigDecimal("16.98")))
                .addPrice(new Price(new BigDecimal("11.54")))
                .addPrice(new Price(new BigDecimal("9")));

            PRODUCTS_SAMPLE.get(1)
                .addPrice(new Price(new BigDecimal("5.2")))
                .addPrice(new Price(new BigDecimal("1.39")))
                .addPrice(new Price(new BigDecimal("8.75")));

            PRODUCTS_SAMPLE.get(2)
                .addPrice(new Price(new BigDecimal("11.73")))
                .addPrice(new Price(new BigDecimal("23.17")))
                .addPrice(new Price(new BigDecimal("17.88")));

            PRODUCTS_SAMPLE.get(3)
                .addPrice(new Price(new BigDecimal("19.55")))
                .addPrice(new Price(new BigDecimal("13.57")))
                .addPrice(new Price(new BigDecimal("29.49")));

            PRODUCTS_SAMPLE.get(4)
                .addPrice(new Price(new BigDecimal("29.41")))
                .addPrice(new Price(new BigDecimal("9.69")))
                .addPrice(new Price(new BigDecimal("29.50")));

            PRODUCTS_SAMPLE.get(5)
                .addPrice(new Price(new BigDecimal("4.55")))
                .addPrice(new Price(new BigDecimal("8.66")))
                .addPrice(new Price(new BigDecimal("15.69")));

            PRODUCTS_SAMPLE.get(6)
                .addPrice(new Price(new BigDecimal("6.17")))
                .addPrice(new Price(new BigDecimal("13.76")))
                .addPrice(new Price(new BigDecimal("1.57")));

            PRODUCTS_SAMPLE.get(7)
                .addPrice(new Price(new BigDecimal("11.73")))
                .addPrice(new Price(new BigDecimal("23.17")))
                .addPrice(new Price(new BigDecimal("17.88")));

            PRODUCTS_SAMPLE.get(8)
                .addPrice(new Price(new BigDecimal("10.16")))
                .addPrice(new Price(new BigDecimal("1.93")))
                .addPrice(new Price(new BigDecimal("24.08")));

            PRODUCTS_SAMPLE.get(9)
                .addPrice(new Price(new BigDecimal("3.76")))
                .addPrice(new Price(new BigDecimal("20.42")))
                .addPrice(new Price(new BigDecimal("21.58")));

            PRODUCTS_SAMPLE.get(10)
                .addPrice(new Price(new BigDecimal("28.27")))
                .addPrice(new Price(new BigDecimal("4.82")))
                .addPrice(new Price(new BigDecimal("16.56")));

            PRODUCTS_SAMPLE.get(11)
                .addPrice(new Price(new BigDecimal("15.42")))
                .addPrice(new Price(new BigDecimal("8.58")))
                .addPrice(new Price(new BigDecimal("11.02")));

            PRODUCTS_SAMPLE.get(12)
                .addPrice(new Price(new BigDecimal("14.59")))
                .addPrice(new Price(new BigDecimal("1.62")))
                .addPrice(new Price(new BigDecimal("16.85")));

            PRODUCTS_SAMPLE.get(13)
                .addPrice(new Price(new BigDecimal("10.83")))
                .addPrice(new Price(new BigDecimal("8.51")))
                .addPrice(new Price(new BigDecimal("1.53")));

            PRODUCTS_SAMPLE.get(14)
                .addPrice(new Price(new BigDecimal("14.63")))
                .addPrice(new Price(new BigDecimal("27.56")))
                .addPrice(new Price(new BigDecimal("11.63")));

            PRODUCTS_SAMPLE.get(15)
                .addPrice(new Price(new BigDecimal("24.45")))
                .addPrice(new Price(new BigDecimal("6.02")))
                .addPrice(new Price(new BigDecimal("17.56")));

            PRODUCTS_SAMPLE.get(16)
                .addPrice(new Price(new BigDecimal("4.83")))
                .addPrice(new Price(new BigDecimal("27.96")))
                .addPrice(new Price(new BigDecimal("29.25")));

            PRODUCTS_SAMPLE.get(17)
                .addPrice(new Price(new BigDecimal("29.52")))
                .addPrice(new Price(new BigDecimal("5.90")))
                .addPrice(new Price(new BigDecimal("10.59")));
        }
}
