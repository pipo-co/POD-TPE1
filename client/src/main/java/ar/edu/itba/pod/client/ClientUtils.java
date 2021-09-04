package ar.edu.itba.pod.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class ClientUtils {
    public static final String DEFAULT_REGISTRY_HOST       = "localhost";
    public static final int    DEFAULT_REGISTRY_PORT       = Registry.REGISTRY_PORT;
    public static final char   ADDRESS_DELIM               = ':';
    public static final String DEFAULT_REGISTRY_ADDRESS    = DEFAULT_REGISTRY_HOST + ADDRESS_DELIM + DEFAULT_REGISTRY_PORT;

    private ClientUtils() {
        // static class
    }

    public static Registry getRegistry(final String registryAddress) throws RemoteException {
        final int addressDelimIdx = registryAddress.indexOf(ADDRESS_DELIM);

        final String    host = addressDelimIdx >= 0 ? registryAddress.substring(0, addressDelimIdx)                     : DEFAULT_REGISTRY_HOST;
        final int       port = addressDelimIdx >= 0 ? Integer.parseInt(registryAddress.substring(addressDelimIdx + 1))  : DEFAULT_REGISTRY_PORT;

        return LocateRegistry.getRegistry(host, port);
    }

}
