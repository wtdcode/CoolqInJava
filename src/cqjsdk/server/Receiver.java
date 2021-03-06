package cqjsdk.server;

import cqjsdk.msg.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/*
类名：接收器
作用：接收CQ发送过来的消息，放入消息队列中。
 */
public class Receiver extends Thread{

    private DatagramSocket server;
    private Dispatcher dispatcher;
    private boolean toStop;
    private boolean stopped;

    Receiver(DatagramSocket server, Dispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
        toStop = false;
        stopped = false;
    }

    public Boolean stopped(){
        return stopped;
    }

    public void run(){
        byte[] buf = new byte[65536];
        Formatter formatter = Formatter.getFormatter();
        Msg msg;
        while(!toStop){
            DatagramPacket msgpacket = new DatagramPacket(buf, buf.length);
            try {
                server.receive(msgpacket);
                msg = formatter.FormatRecv(msgpacket.getData(), msgpacket.getLength());
                Logger.Log("接收一条"+msg.getPrefix());
                dispatcher.dispatch(msg);
            }
            catch (SocketException | NullPointerException socketEx){
                if(!server.isClosed()){
                    socketEx.printStackTrace();
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        stopped = true;
    }

    public void die(){
        toStop = true;
        server.close();
    }
}