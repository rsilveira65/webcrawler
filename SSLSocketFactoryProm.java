import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// referencia http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLSocketFactory.html
public class SSLSocketFactoryProm extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public SSLSocketFactoryProm() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, IOException, CertificateException {
        super();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        
        TrustManager tm;
        tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
    
    
    @Override
    public String[] getSupportedCipherSuites(){
        SSLSocketFactory  d;
        d = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return d.getSupportedCipherSuites();      
    }
    
    @Override
    public String[] getDefaultCipherSuites(){
        SSLSocketFactory  d;
        d = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return d.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    


   
    
}