package com.api;

import com.api.entity.Price;
import com.api.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class Resources {

    public static final List<Product> PRODUCTS_SAMPLE = List.of(
        Product.builder()
            .id(UUID.fromString("37a89eba-dd3b-4179-9538-d9e5f720fc11"))
            .description("ACHOC PO NESCAU 800G")
            .sequenceCode(29250)
            .barcode("7891000055120")
            .build(),
        Product.builder()
            .id(UUID.fromString("311534bd-e4b4-4d90-b23a-9d0f0ecb1759"))
            .description("AMENDOIM SALG CROKISSIMO 400G PIMENTA")
            .sequenceCode(120983)
            .barcode("7896336010058")
            .build(),
        Product.builder()
            .id(UUID.fromString("d1751039-9ec8-4867-bc08-9e4297724984"))
            .description("BALA GELATINA FINI 500G BURGUER")
            .barcode("78982797922990")
            .sequenceCode(93556)
            .build(),
        Product.builder()
            .id(UUID.fromString("dbbed06a-d8d4-4e0f-81e3-bd7f63e0a59b"))
            .description("BISC ROSQ MARILAN 350G INT")
            .barcode("7896003737257")
            .sequenceCode(127635)
            .build(),
        Product.builder()
            .id(UUID.fromString("378be7fc-4a82-4730-9535-05a842bd2fd2"))
            .description("BISC WAFER TODDY 132G CHOC")
            .barcode("7896071024709")
            .sequenceCode(122504)
            .build(),
        Product.builder()
            .id(UUID.fromString("e46de01e-2e42-4b2f-80ec-ba4b828836e9"))
            .description("BISC ZABET 350G LEITE")
            .barcode("7896085087028")
            .sequenceCode(144038)
            .build(),
        Product.builder()
            .id(UUID.fromString("98a4139e-241e-4e70-be64-a5af532203cd"))
            .description("BOLINHO BAUDUC 40G GOTAS CHOC")
            .barcode("7891962037219")
            .sequenceCode(98894)
            .build(),
        Product.builder()
            .id(UUID.fromString("6f352d2b-1b6c-41d5-a031-7c04b70f4ebf"))
            .description("CAFE UTAM 500G")
            .sequenceCode(2909)
            .barcode("7896656800018")
            .build(),
        Product.builder()
            .id(UUID.fromString("b21792b9-0f50-46bf-b92f-c7d311b5adf4"))
            .description("LEITE PO NINHO 400G INTEG")
            .barcode("7891000000427")
            .sequenceCode(892)
            .build(),
        Product.builder()
            .id(UUID.fromString("a66a99e4-033f-4c5f-a7ba-1271a0c3ec61"))
            .description("LIMP M.USO OMO 500ML DESINF HERBAL")
            .barcode("7891150080850")
            .sequenceCode(141947)
            .build(),
        Product.builder()
            .id(UUID.fromString("d63bfe28-ad95-4493-beb6-11f640d83bec"))
            .description("MAIONESE QUERO 210G TP")
            .barcode("7896102513714")
            .sequenceCode(87689)
            .build(),
        Product.builder()
            .id(UUID.fromString("d5867d35-700d-4119-bb6e-465638b366f0"))
            .description("MILHO VDE PREDILECTA 170G LT")
            .barcode("7896292340503")
            .sequenceCode(134049)
            .build(),
        Product.builder()
            .id(UUID.fromString("f28763c9-d464-4f25-b495-b554fe62c3b4"))
            .description("OLEO MILHO LIZA 900ML")
            .barcode("7896036090619")
            .sequenceCode(5648)
            .build(),
        Product.builder()
            .id(UUID.fromString("25ab0abc-29d6-4248-b0c4-c08a397e95f2"))
            .description("PAP ALUMINIO WYDA 30X7.5")
            .barcode("7898930672441")
            .sequenceCode(30881)
            .build(),
        Product.builder()
            .id(UUID.fromString("5540ac81-43f9-490f-8b32-e522c9d9ea9e"))
            .description("PAP HIG F.D NEVE C8 COMPACTO NEUT")
            .barcode("7891172422379")
            .sequenceCode(25336)
            .build(),
        Product.builder()
            .id(UUID.fromString("8b5e7d3e-df06-44f4-82c1-cb13d654da00"))
            .description("REFRIG ANTARCT 600ML PET GUARANA")
            .barcode("7891991002646")
            .sequenceCode(6367)
            .build(),
        Product.builder()
            .id(UUID.fromString("272afb09-4994-4a5a-a4a1-ea2b20af112b"))
            .description("SAL MARINHO LEBRE 500G GOURMET")
            .barcode("7896110195162")
            .sequenceCode(128177)
            .build(),
        Product.builder()
            .id(UUID.fromString("34560531-6f7c-451c-b2ce-9de26e4d4c6c"))
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
