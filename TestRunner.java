public class TestRunner {
    public static void main(String[] args) {
        Queries queries = new Queries();
//        System.out.println(queries.getAnimeColumns(true, true, true, true, false, true));
//        System.out.println(queries.getAnimeByStudio("MAPPA"));
//        System.out.println(queries.filterAnime("author ID = 1"));
//        System.out.println(queries.getStudiosWithMultipleAnime());
//        System.out.println(queries.getAnimeReviewedByAllUsers());
        System.out.println(queries.getAnimeAttributes("Tenki no Ko"));
        queries.closeDatabase();
    }
}
