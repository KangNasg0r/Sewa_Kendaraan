package form;

public class UserID {

    private static String id;

    static void setIdKasir(String idKasir) {
        UserID.id = idKasir;
    }

    public static String getIdKasir() {
        return id;
    }
}
