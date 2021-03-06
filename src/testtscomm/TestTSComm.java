/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testtscomm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ECDHUPublicParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import packets.HandshakeUtils;
import packets.HighLevelPacket;
import packets.LowLevelClientPacket;
import packets.LowLevelPacket;
import packets.LowLevelServerPacket;
import packets.highlevel.InitPacket;
import packets.payloads.InitPayload;
import packets.payloads.handshake.HandShakePayload0;
import packets.payloads.handshake.HandShakePayload1;
import packets.payloads.handshake.HandShakePayload2;
import packets.payloads.handshake.HandShakePayload3;
import packets.payloads.handshake.HandShakePayload4;
import testtscomm.crypto.CryptoSession;
import testtscomm.crypto.CryptoUtils;
import testtscomm.crypto.KeyNonce;

/**
 *
 * @author bowen
 */
public class TestTSComm {
    
    private static final Random random = new Random(1363134);
    /**
     * @param args the command line arguments
     */
    
    
    public static void genTest() throws UnknownHostException, SocketException {
        
        DatagramSocket socket = new DatagramSocket(51008, InetAddress.getLocalHost());
        SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 9987);
        
        
        InitPacket<HandShakePayload0> packet0 = new InitPacket(new HandShakePayload0(random));
        
        InitPacket<HandShakePayload1> packet1 = new InitPacket(new HandShakePayload1(payload, packet0.getPayload()));
        
    }
    
    public static void test() {
        
        String data = "TS3INIT1     clientinitiv alpha={test==} omega={test=} ip";
        byte[] command = data.getBytes(StandardCharsets.US_ASCII);
        byte[] header = new byte[] {0x0, 0x0, 0x0, 0x0, 0x2};
        byte[] header2 = new byte[] {0x1, 0x3, 0x0, 0x0, 0x2};
        
        KeyNonce keyNonce = CryptoUtils.DUMMYPAIR;
        EAXBlockCipher eaxCipher = new EAXBlockCipher(new AESEngine());
        CipherParameters parameters = new AEADParameters(new KeyParameter(keyNonce.getKey()), CryptoUtils.MAC_LENGTH * 8, keyNonce.getNonce(), header);
        
        
        byte[] result;
        int len;
        eaxCipher.init(true, parameters);
        result = new byte[eaxCipher.getOutputSize(command.length)];
        try {
            len = eaxCipher.processBytes(command, 0, command.length, result, 0);
            len += eaxCipher.doFinal(result, len);
        } catch (Exception ex) {
            throw new IllegalStateException("Internal encryption error!");
        }
        
        parameters = new AEADParameters(new KeyParameter(keyNonce.getKey()), CryptoUtils.MAC_LENGTH * 8, keyNonce.getNonce(), header2);
        
        byte[] decryptResult;
        
        try {
            eaxCipher.init(false, parameters);
            decryptResult = new byte[eaxCipher.getOutputSize(result.length)];

            len = 0;
            len += eaxCipher.processBytes(result, 0, result.length, decryptResult, 0);
            len += eaxCipher.doFinal(decryptResult, len);
            System.out.println(new String(decryptResult));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        
        
        DatagramSocket socket = new DatagramSocket(51000, InetAddress.getLocalHost());
        SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 9987);
        
        socket.setSoTimeout(5000);
        
        HandShakePayload0 payload0 = new HandShakePayload0(random);
        System.out.println("Packet 0 Encode:");
        System.out.println(Arrays.toString(payload0.getStampCopy()));
        System.out.println(Arrays.toString(payload0.getUnixTimeCopy()));
        System.out.println(Arrays.toString(payload0.getRandomBytesA0Copy()));
        
        LowLevelPacket packet0 = HandshakeUtils.encode(payload0);
        
        System.out.println(Arrays.toString(packet0.getPayloadCopy()));
        System.out.println(Arrays.toString(packet0.getRaw()));
        
        socket.send(new DatagramPacket(packet0.getRaw(), packet0.length(), address));
        
        DatagramPacket dp1 = new DatagramPacket(new byte[500], 500);
        socket.receive(dp1);
        LowLevelServerPacket packet1 = new LowLevelServerPacket();
        packet1.setRaw(Arrays.copyOf(dp1.getData(), dp1.getLength()));
        
        InitPacket<HandShakePayload1> ipacket1 = HandshakeUtils.decode1(packet1, payload0);
        System.out.println(Arrays.toString(packet1.getRaw()));
        System.out.println(Arrays.toString(packet1.getPayloadCopy()));
        
        System.out.println("Packet 1 Decode:");
        System.out.println(Arrays.toString(packet1.getRaw()));
        System.out.println(Arrays.toString(packet1.getPayloadCopy()));
        System.out.println(Arrays.toString(ipacket1.getPayload().getA1Copy()));
        System.out.println(Arrays.toString(ipacket1.getPayload().getReversedA0Copy()));
        
        HandShakePayload2 payload2 = new HandShakePayload2(ipacket1.getPayload());
        LowLevelPacket packet2 = HandshakeUtils.encode(payload2);
        
        System.out.println("Packet 2 Encode:");
        System.out.println(Arrays.toString(payload2.getStampCopy()));
        System.out.println(Arrays.toString(payload2.getA1Copy()));
        System.out.println(Arrays.toString(payload2.getA0ReversedCopy()));
        System.out.println(Arrays.toString(packet2.getPayloadCopy()));
        System.out.println(Arrays.toString(packet2.getRaw()));
        
        socket.send(new DatagramPacket(packet2.getRaw(), packet2.length(), address));
        
        
        DatagramPacket dp3 = new DatagramPacket(new byte[500], 500);
        socket.receive(dp3);
        LowLevelServerPacket packet3 = new LowLevelServerPacket();
        packet3.setRaw(Arrays.copyOf(dp3.getData(), dp3.getLength()));
        
        HandShakePayload3 payload3 = new HandShakePayload3(packet3.getPayloadCopy());
        
        System.out.println("Packet 3 Decode:");
        System.out.println(Arrays.toString(packet3.getRaw()));
        System.out.println(Arrays.toString(packet3.getPayloadCopy()));
        System.out.println(packet3.getPayloadLength());
        System.out.println(payload3.getX());
        System.out.println(payload3.getN());
        System.out.println(payload3.getLevel());
        
        String initivString = "clientinitiv alpha=p1rc4MMk69PMbQ== omega=MEsDAgcAAgEgAiBaLaaLJjCDMyh5EVq1mhQu8KTHUhj5jX388UZqhxS1VQIgXWrxWBA+WveQrBuEoQvFuRTMAFiOTb+KIhe5ypGnyQc= ip";
        
        HandShakePayload4 payload4 = new HandShakePayload4(payload3, initivString.getBytes(StandardCharsets.US_ASCII));
        //HandShakePayload4 payload4 = new HandShakePayload4(payload3, new byte[0]);
        
        System.out.println(payload4.getY());
        LowLevelClientPacket packet4 = HandshakeUtils.encode(payload4);
        socket.send(new DatagramPacket(packet4.getRaw(), packet4.length(), address));
        
        DatagramPacket dp5 = new DatagramPacket(new byte[500], 500);
        socket.receive(dp5);
        LowLevelServerPacket packet5 = new LowLevelServerPacket();
        packet5.setRaw(Arrays.copyOf(dp5.getData(), dp5.getLength()));
        
        System.out.println(new String(packet5.getRaw()));
        
        //genTest();
        return;
        
        DatagramSocket socket = new DatagramSocket(51008, InetAddress.getLocalHost());
        SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 9987);
        

        Date d = new Date();
        int unixTime = (int)d.getTime() / 1000;
        
        //Send packet 0
        byte[] mac = new byte[] {0x54, 0x53, 0x33, 0x49, 0x4E, 0x49, 0x54, 0x31};
        byte[] data = new byte[] {0x06, 0x3B, (byte)0xEC, (byte)0xE9, 0x00, (byte)((unixTime >> 24) & 0xFF), (byte)((unixTime >> 16) & 0xFF), (byte)((unixTime >> 8) & 0xFF), (byte)(unixTime & 0xFF), 1, 1, 1, 1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ClientPacket packet0 = new ClientPacket(mac, (short)101, (short)0, (short)0, (byte)0x88, data);
        
        socket.send(new DatagramPacket(packet0.getRaw(), packet0.getRaw().length, address));

        //Receive packet 1
        ServerPacket packet1 = new ServerPacket(1+16+4, (short)0);
        socket.receive(new DatagramPacket(packet1.getRaw(), packet1.getRaw().length));
        
        byte[] p1p = packet1.getPayloadCopy();
        data = new byte[] {0x06, 0x3B, (byte)0xEC, (byte)0xE9, 0x02, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        for (int i=0; i<20; i++) {
            data[i+5] = p1p[i+1];
        }
        ClientPacket packet2 = new ClientPacket(mac, (short)101, (short)0, (short)0, (byte)0x88, data);
        socket.send(new DatagramPacket(packet2.getRaw(), packet2.getRaw().length, address));
        
        ServerPacket packet3 = new ServerPacket(1+64+64+4+100, (short)0);
        socket.receive(new DatagramPacket(packet3.getRaw(), packet3.getRaw().length));
        
        byte[] level = new byte[4];
        //System.arraycopy(packet3.getPayload(), 1+64+64, level, 0, 4);
        System.arraycopy(packet3.getRaw(), 11+1+64+64, level, 0, 4);
        
        int v = 0;
        data = new byte[4+1+64+64+4+100+64+v];
        System.arraycopy(new byte[] {0x06, 0x3B, (byte)0xEC, (byte)0xE9, 0x04}, 0, data, 0, 5);
        System.arraycopy(packet3.getPayloadCopy(), 1, data, 5, 64+64+4+100);
        
        
        System.out.println((int)byteArrToLong(level));
        BigInteger ex = new BigInteger("2");
        ex = ex.pow((int)byteArrToLong(level));
        
        BigInteger x = byteArrToBigInteger(Arrays.copyOfRange(packet3.getPayloadCopy(), 1, 1+64));
        BigInteger n = byteArrToBigInteger(Arrays.copyOfRange(packet3.getPayloadCopy(), 1+64, 1+64+64));
        
        System.out.println(x);
        System.out.println(n);
        
        BigInteger y = x.modPow(ex, n);
        byte[] yByte = y.toByteArray();
        
        System.out.println(y);
        
        if (yByte.length < 64) {
            byte[] tempYByte = new byte[64];
            System.arraycopy(yByte, 0, tempYByte, 64 - yByte.length, yByte.length);
            yByte = tempYByte;
        }
        System.arraycopy(yByte, 0, data, 4+1+64+64+4+100+(64-yByte.length), yByte.length);
        
        
        
        CryptoSession session = new CryptoSession();
        
        session.setIdentity(CryptoUtils.generateTestIdentity(8));
        byte[] alpha = new byte[10];
        random.nextBytes(alpha);
        String initData = "clientinitiv alpha=" + new String(Base64.getEncoder().encode(alpha), StandardCharsets.US_ASCII) + " omega=" + session.getIdentity().getPublicKeyString() + " ip";
        
        byte[] command = initData.getBytes(StandardCharsets.US_ASCII);
        
        byte[] dataCommand = new byte[data.length + command.length];
        
        System.arraycopy(data, 0, dataCommand, 0, data.length);
        System.arraycopy(command, 0, dataCommand, data.length, command.length);
        
        ClientPacket packet4 = new ClientPacket(mac, (short)101, (short)0, (short)0, (byte)0x88, dataCommand);
        socket.send(new DatagramPacket(packet4.getRaw(), packet4.getRaw().length, address));
        
        
        ClientPacket packet5 = new ClientPacket(mac, (short)0, (short)0, (short)0, (byte)0x02, command);
        session.encrypt(packet5);
        //socket.send(new DatagramPacket(packet5.getRaw(), packet5.getRaw().length, address));
        System.out.println("SENT PACKET 5");
        
        session.decrypt(packet5);
        System.out.println(new String(packet5.getRaw()));
        
        ServerPacket packet6 = new ServerPacket(500, (short)0);
        DatagramPacket datagram6 = new DatagramPacket(packet6.getRaw(), packet6.getRaw().length);
        socket.receive(datagram6);        
        packet6.setRaw(Arrays.copyOfRange(packet6.getRaw(), 0, datagram6.getLength()));
        System.out.println(new String(packet6.getPayloadCopy()));
        System.out.println(session.decrypt(packet6));
        System.out.println(new String(packet6.getPayloadCopy()));
        
        
        ClientPacket packet6Ack = new ClientPacket(new byte[8], (short)0, (short)0, (short)0, (byte)0x06, new byte[] {(byte)(packet6.getPid() >> 8), (byte)(packet6.getPid() & 0xFF)});
        session.encrypt(packet6Ack);
        socket.send(new DatagramPacket(packet6Ack.getRaw(), packet6Ack.getRaw().length, address));
        socket.send(new DatagramPacket(packet6Ack.getRaw(), packet6Ack.getRaw().length, address));
        socket.send(new DatagramPacket(packet6Ack.getRaw(), packet6Ack.getRaw().length, address));
        
        String abo = new String(packet6.getPayloadCopy()).replace("\\", "");
        System.out.println(abo);
        String alphaString = subStringSearch(abo, "alpha=", " ");
        String betaString = subStringSearch(abo, "beta=", " ");
        String omegaString = subStringSearch(abo, "omega=", " ").replace("\\", "");
        Base64.getDecoder().decode(alphaString);
        Base64.getDecoder().decode(betaString);
        Base64.getDecoder().decode(omegaString);
        session.initKey(subStringSearch(abo, "alpha=", " "), subStringSearch(abo, "beta=", " "), omegaString);
        
        String clientInfo = "clientinit client_nickname=test client_version=3.0.19.3 [Build: 1466672534] client_platform=Windows client_version_sign=a1OYzvM18mrmfUQBUgxYBxYz2DUU6y5k3/mEL6FurzU0y97Bd1FL7+PRpcHyPkg4R+kKAFZ1nhyzbgkGphDWDg== client_key_offset=0 hwid=3613432";
        
        ClientPacket packet7 = new ClientPacket(new byte[8], (short)0, (short)0, (short)0, (byte)0x22, clientInfo.getBytes(StandardCharsets.US_ASCII));
        session.encrypt(packet7);
        socket.send(new DatagramPacket(packet7.getRaw(), packet7.getRaw().length, address));
        
        
        ServerPacket packet8 = new ServerPacket(500, (short)0);
        DatagramPacket datagram8 = new DatagramPacket(packet8.getRaw(), packet8.getRaw().length);
        socket.receive(datagram8);
        packet6.setRaw(Arrays.copyOfRange(packet8.getRaw(), 0, datagram8.getLength()));
        System.out.println(Arrays.toString(packet8.getHeaderCopy()));
        System.out.println(session.decrypt(packet8));
        System.out.println(new String(packet8.getPayloadCopy()));
        
        socket.close();
        /*
        Socket socket = new Socket(InetAddress.getLocalHost(), 15580);
        OutputStream toServer = socket.getOutputStream();
        InputStream fromServer = socket.getInputStream();
        
        toServer.write(53);
        
        toServer.write("test".getBytes());
        
        Thread.sleep(1000);
        toServer.close();
        fromServer.close();
        */
    }
    
    public static String subStringSearch(String s, String begin, String end) {
        int beginIndex = s.indexOf(begin) + begin.length();
        int endIndex = s.indexOf(end, beginIndex);
        if (endIndex == -1) {
            endIndex = s.length();
        }
        return s.substring(beginIndex, endIndex);
    }
    
    public static long byteArrToLong(byte[] number) {
        long value = 0;
        for (int i = 0; i<number.length; i++) {
            value = (value << 8) + (number[i] & 0xff);
        }
        return value;
    }
    public static BigInteger byteArrToBigInteger(byte[] number) {
        BigInteger value = new BigInteger("0");
        for (int i = 0; i < number.length; i++) {
            value = value.shiftLeft(8).add(new BigInteger("" + (number[i] & 0xFF)));
        }
        return value;
    }
    public static int totient(int num){ //euler's totient function calculator. returns totient
    int count=0;
    for(int a=1;a<num;a++){ //definition of totient: the amount of numbers less than num coprime to it
      if(GCD(num,a)==1){ //coprime
        count++;
      }
    }
    return(count);
 }
public static int GCD(int a, int b){ //faster euclidean algorithm-see GCD for explanation
    int temp;
    if(a<b){
      temp=a;
      a=b;
      b=temp;
    }
    if(a%b==0){
      return(b);
    }
    return(GCD(a%b,b));
  }
    
}
