package pt.ulisboa.tecnico.cmov.pharmacist.client;

public class APIFactory {
    private static APIInterface apiInterface;

    public static APIInterface getInterface() {
        if (apiInterface == null) {
            apiInterface = APIClient.getClient().create(APIInterface.class);
        }

        return apiInterface;
    }
}
