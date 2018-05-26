package org.dimdev.utils;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public final class SSLUtils {

    /** Disables SSL certificate validation. */
    public static void trustAllCertificates() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                @Override public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** Trusts certificates in a key store on top of the ones currently trusted by wrapping the TrustManager */
    public static void trustCertificates(KeyStore keyStore) {
        try {
            // Init TFM with default trust store
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            final X509TrustManager defaultTrustManager = getX509TrustManager(trustManagerFactory);

            // Init TMF with new trust store
            trustManagerFactory.init(keyStore);
            final X509TrustManager customTrustManager = getX509TrustManager(trustManagerFactory);

            // Create a trust manager that wraps the default one
            X509TrustManager wrappingTrustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return joinArrays(defaultTrustManager.getAcceptedIssuers(), customTrustManager.getAcceptedIssuers());
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    try {
                        customTrustManager.checkServerTrusted(chain, authType);
                    } catch (CertificateException e) {
                        defaultTrustManager.checkServerTrusted(chain, authType);
                    }
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    try {
                        customTrustManager.checkClientTrusted(chain, authType);
                    } catch (CertificateException e) {
                        defaultTrustManager.checkClientTrusted(chain, authType);
                    }
                }
            };

            // Replace the default SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{wrappingTrustManager}, null);
            SSLContext.setDefault(sslContext);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static X509TrustManager getX509TrustManager(TrustManagerFactory trustManagerFactory) {
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        throw new RuntimeException("Failed to find X509TrustManager");
    }

    private static <T> T[] joinArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static KeyStore loadKeyStoreFromFile(File keystoreFile, String password) {
        try (InputStream keyStoreInputStream = new FileInputStream(keystoreFile)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, password.toCharArray());
            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException();
        }
    }
}