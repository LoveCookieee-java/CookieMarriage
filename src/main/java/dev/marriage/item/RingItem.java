package dev.marriage.item;

/**
 * Đại diện cho một item nhẫn cưới được cấu hình trong ring.yml.
 *
 * @param provider Nguồn gốc của item (VANILLA, MMOITEMS, ITEMEDIT, ITEMSADDER)
 * @param type     Type phụ — chỉ dùng cho MMOItems (ví dụ: "RING"). Trống với các provider khác.
 * @param id       ID của item (Material name với VANILLA, hoặc ID chuỗi với các plugin item)
 */
public record RingItem(RingProvider provider, String type, String id) {

    /**
     * Parse một dòng config thành RingItem.
     * Định dạng:
     *   VANILLA;<Material>
     *   MMOITEMS;<TYPE>;<ID>
     *   ITEMEDIT;<ID>
     *   ITEMSADDER;<namespace:id>
     *
     * @param raw Dòng config thô từ ring.yml
     * @return RingItem đã parse, hoặc null nếu định dạng không hợp lệ
     */
    public static RingItem parse(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String[] parts = raw.trim().split(";");
        if (parts.length < 2) return null;

        String providerStr = parts[0].toUpperCase();
        RingProvider provider;
        try {
            provider = RingProvider.valueOf(providerStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return switch (provider) {
            case VANILLA    -> new RingItem(provider, "", parts[1].toUpperCase());
            case ITEMEDIT   -> new RingItem(provider, "", parts[1]);
            case ITEMSADDER -> new RingItem(provider, "", parts[1]);
            case MMOITEMS   -> parts.length >= 3
                    ? new RingItem(provider, parts[1].toUpperCase(), parts[2])
                    : null;
        };
    }

    /** Enum các nguồn gốc item được hỗ trợ. */
    public enum RingProvider {
        VANILLA,
        MMOITEMS,
        ITEMEDIT,
        ITEMSADDER
    }
}
