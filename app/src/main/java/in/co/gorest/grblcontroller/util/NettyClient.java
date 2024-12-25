package in.co.gorest.grblcontroller.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import cn.wandersnail.commons.util.ShellUtils;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.model.PrinterFiles;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyClient {
    private static final String M997_IDLE = "M997 IDLE";
    private static final String M997_PAUSE = "M997 PAUSE";
    private static final String M997_PRINTING = "M997 PRINTING";
    private static String command = "";
    private static List<PrinterFiles> datas = null;
    private static String espFileString = "";
    private static Handler handler = null;
    private static int hasSendCount = 0;
    private static boolean isJob = false;
    private static int isclosingLightCount = 0;
    private static boolean mIsBackground = false;
    private static String mhost = "";
    private static NettyClient nettyClient;
    private Channel channel;
    private EventLoopGroup group;
    private boolean isConnect = false;
    private boolean getStatus = true;
    private boolean isPrinting = false;
    private int reconnectNum = 9999;
    private long reconnectIntervalTime = 5000;
    private boolean ishandleDisconnect = false;

    public static NettyClient getInstance(Handler handler2) {
        handler = handler2;
        if (nettyClient == null) {
            nettyClient = new NettyClient();
        }
        return nettyClient;
    }

    public static NettyClient getInstance() {
        if (nettyClient == null) {
            nettyClient = new NettyClient();
        }
        return nettyClient;
    }

    public void setHandler(Handler handler2) {
        handler = handler2;
    }

    public void setIsBackground(Boolean bool) {
        mIsBackground = bool.booleanValue();
    }

    public synchronized NettyClient connect(final String str, final int i) {
        this.ishandleDisconnect = false;
        hasSendCount = 0;
        this.isPrinting = false;
        PrefUtil.getInstance().put("isclosingLight", false);
        if (!this.isConnect) {
            this.group = new NioEventLoopGroup();
            try {
                new Bootstrap().group(this.group).option(ChannelOption.SO_KEEPALIVE, true).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() { // from class: makerbase.com.mkslaser.netty.NettyClient.2
                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // io.netty.channel.ChannelInitializer
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        socketChannel.pipeline().addLast("basedFrameDecoder", new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(ShellUtils.COMMAND_LINE_END.getBytes())));
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast("ping", new IdleStateHandler(0L, 0L, 180L, TimeUnit.SECONDS));
                        pipeline.addLast("requestDecoder", new HttpRequestDecoder());
                        pipeline.addLast("responseEncoder", new HttpResponseEncoder());
                        pipeline.addLast("deflater", new HttpContentCompressor());
                    }
                }).handler(new SimpleChannelInboundHandler<ByteBuf>() { // from class: makerbase.com.mkslaser.netty.NettyClient.1
                    @Override // io.netty.channel.ChannelInboundHandlerAdapter, io.netty.channel.ChannelInboundHandler
                    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
                        NettyClient.getInstance(NettyClient.handler).setConnectStatus(true);
                        PrefUtil.getInstance().put("currentStatus", "IDLE");
                        Log.i("NettyClient", "channelActive: 连接上服务器");
                        String unused = NettyClient.mhost = str;
                        Message message = new Message();
                        message.what = 6;
                        NettyClient.handler.sendMessage(message);
                        NettyClient.this.handleMessage(6, "");
                    }

                    @Override // io.netty.channel.ChannelInboundHandlerAdapter, io.netty.channel.ChannelInboundHandler
                    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
                        Log.i("NettyClient", "channelActive: 未连接上服务器");
                        String unused = NettyClient.mhost = "";
                        NettyClient.getInstance(NettyClient.handler).setConnectStatus(false);
                        NettyClient.getInstance(NettyClient.handler).reconnect(str, i);
                        Message message = new Message();
                        message.what = 0;
                        NettyClient.handler.sendMessage(message);
                    }

                    /* JADX INFO: Access modifiers changed from: protected */
                    @Override // io.netty.channel.SimpleChannelInboundHandler
                    public void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        String byteBuf2 = Unpooled.copiedBuffer(byteBuf).toString(Charset.forName("utf-8"));
                        Log.i("NettyClient", "服务器返回-->" + byteBuf2);
                        Message message = new Message();
                        message.what = 2;
                        message.obj = byteBuf2;
                        NettyClient.handler.sendMessage(message);
                        NettyClient.this.handleMessage(2, byteBuf2);
                        int unused = NettyClient.hasSendCount = 0;

                        // 传递消息
                        EventBus.getDefault().post(new ServiceMessageEvent(byteBuf2));
                    }
                }).connect(str, i).addListener((GenericFutureListener<? extends Future<? super Void>>) new ChannelFutureListener() { // from class: makerbase.com.mkslaser.netty.NettyClient.3
                    @Override // io.netty.util.concurrent.GenericFutureListener
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            NettyClient.this.isConnect = true;
                            NettyClient.this.channel = channelFuture.channel();
                            return;
                        }
                        NettyClient.this.isConnect = false;
                    }
                }).sync();
            } catch (Exception e) {
                Log.i("Exception-----", e.getMessage());
                mhost = "";
                e.printStackTrace();
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        return this;
    }

    public void disconnect() {
        this.group.shutdownGracefully();
        try {
            this.channel.closeFuture().sync();
            this.channel.close();
        } catch (Exception e) {
            Log.i("disconnect--e---", e.getMessage());
        }
        mhost = "";
        hasSendCount = 0;
        Message message = new Message();
        message.what = 7;
        message.obj = "disconnect";
        handler.sendMessage(message);
    }

    public void reconnect(String str, int i) {
        int i2 = this.reconnectNum;
        if (i2 > 0 && !this.isConnect) {
            this.reconnectNum = i2 - 1;
            try {
                Thread.sleep(this.reconnectIntervalTime);
            } catch (InterruptedException unused) {
            }
            disconnect();
            connect(str, i);
            return;
        }
        if (this.ishandleDisconnect) {
            handleMessage(8, "");
        } else {
            handleMessage(7, "");
        }
        disconnect();
    }

    public boolean sendMsgToServer(byte[] bArr, ChannelFutureListener channelFutureListener) {
        boolean z = this.channel != null && this.isConnect;
        if (!engraveUtils.CMDWithoutOK(bArr)) {
            hasSendCount++;
        }
        if (hasSendCount > 30) {
            disconnect();
        }
        if (z) {
            ByteBuf copiedBuffer = Unpooled.copiedBuffer(bArr);
            Log.i("sendMsgToServer---", new String(bArr));
            if (channelFutureListener != null) {
                this.channel.writeAndFlush(copiedBuffer).addListener((GenericFutureListener<? extends Future<? super Void>>) channelFutureListener);
            } else {
                this.channel.writeAndFlush(copiedBuffer);
            }
        }
        return z;
    }

    public void setReconnectNum(int i) {
        this.reconnectNum = i;
    }

    public void setReconnectIntervalTime(long j) {
        this.reconnectIntervalTime = j;
    }

    public boolean getConnectStatus() {
        return this.isConnect;
    }

    public String getHost() {
        return mhost;
    }

    public void setisclosingLightCount(int i) {
        isclosingLightCount = i;
    }

    public void setConnectStatus(boolean z) {
        this.isConnect = z;
    }

    public void handleDisconnect() {
        this.isConnect = false;
        this.reconnectNum = 0;
        this.ishandleDisconnect = true;
        disconnect();
    }

    public void handleMessage(int i, String str) {
        if (i == 0) {
            this.isConnect = false;
            this.reconnectNum = 0;
            this.ishandleDisconnect = true;
            return;
        }
        if (i == 6) {
            sendMsgToServer((PrefUtil.getInstance().get("mainboard", "normal").equals("esp") ? "?" : "M997\n").getBytes(), null);
            getStatusThead();
            return;
        }
        if (i != 2) {
            if (i != 3) {
                return;
            }
            try {
                new JSONObject(str).getBoolean(ToolUtil.JsonKey.JSON_SUCCESS_KEY);
                return;
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        if (PrefUtil.getInstance().get("mainboard", "normal").equals("esp")) {
            dealESPMassage(str);
            return;
        }
        if (str.contains("M997")) {
            dealReturnValue(str);
            return;
        }
        if (str.contains("M27")) {
            dealProgressValue(str);
            return;
        }
        if (str.contains("M994")) {
            dealEngravingName(str);
            return;
        }
        if (str.contains("Begin file list")) {
            command = str.substring(str.indexOf("Begin file list"));
            return;
        }
        if (command.contains("Begin file list")) {
            String str2 = command + str;
            command = str2;
            if (str2.contains("End file list")) {
                List<PrinterFiles> decodeFileList = GcodeHelper.decodeFileList(command);
                datas = decodeFileList;
                dealFliesList(decodeFileList);
                command = "";
                return;
            }
            return;
        }
        PrefUtil.getInstance().put("ReturnValue", str);
    }

    private void dealEngravingName(String str) {
        if (str.contains("M994")) {
            String[] split = str.split(" ");
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    if (split[i].contains(".nc")) {
                        String substring = split[i].substring(split[i].indexOf(":/") + 2, split[i].indexOf(".nc"));
                        PrefUtil.getInstance().put("engravingName", substring + ".nc");
                    }
                }
            }
        }
    }

    private void dealFliesList(List<PrinterFiles> list) {
        String str = "";
        for (int i = 0; i < list.size(); i++) {
            String fileName = list.get(i).getFileName();
            if (fileName.toLowerCase().endsWith(".nc")) {
                str = str + fileName + "#$";
            }
        }
        Log.i("dealFliesList--", str);
        PrefUtil.getInstance().put("currentFiles", str);
    }

    private void dealProgressValue(String str) {
        if (str.contains("M27")) {
            String[] split = str.split(" ");
            if (split.length > 1) {
                PrefUtil.getInstance().put("currentProgress", split[1]);
            }
        }
    }

    private void dealReturnValue(String str) {
        if (str.contains(M997_IDLE)) {
            PrefUtil.getInstance().put("currentStatus", "IDLE");
        } else if (str.contains(M997_PRINTING)) {
            PrefUtil.getInstance().put("currentStatus", "PRINTING");
        } else if (str.contains(M997_PAUSE)) {
            PrefUtil.getInstance().put("currentStatus", "PAUSE");
        }
    }

    private void dealESPMassage(String str) {
        if (str.toLowerCase().contains("error:67")) {
            Message message = new Message();
            message.what = 8;
            message.obj = str;
            handler.sendMessage(message);
        }
        if (str.toLowerCase().contains("jog") && PrefUtil.getInstance().get("isclosingLight", false)) {
            Log.i("isJob---------", "");
            isJob = true;
        } else if (str.toLowerCase().contains("idle") && PrefUtil.getInstance().get("isclosingLight", false) && isJob) {
            Log.i("no isJob---------", "");
            sendMsgToServer("M5\n".getBytes(), null);
            PrefUtil.getInstance().put("isclosingLight", false);
            isJob = false;
        }
        if (PrefUtil.getInstance().get("isgettingFiles", false)) {
            if (espFileString.contains("error 120") || espFileString.contains("error:60") || espFileString.contains("No SD Card")) {
                PrefUtil.getInstance().put("isgettingFiles", false);
            }
            String str2 = espFileString + str;
            espFileString = str2;
            if (str2.contains("[SD Free:")) {
                PrefUtil.getInstance().put("isgettingFiles", false);
                dealespFileString(espFileString);
                espFileString = "";
            }
            Message message2 = new Message();
            message2.what = 4;
            message2.obj = str;
            handler.sendMessage(message2);
            return;
        }
        ToolUtil.getLaserInfoFromString(str);
        Message message3 = new Message();
        message3.what = 5;
        message3.obj = str;
        handler.sendMessage(message3);
        for (String str3 : str.split(ShellUtils.COMMAND_LINE_END)) {
            if (str3.contains("<") && str3.contains(">")) {
                String[] split = str3.split("\\|");
                for (int i = 0; i < split.length; i++) {
                    String str4 = split[i];
                    if (i == 0) {
                        if (str4.toLowerCase().contains("idle") || str4.toLowerCase().contains("jog")) {
                            PrefUtil.getInstance().put("currentStatus", "IDLE");
                            this.isPrinting = false;
                        } else if (str4.toLowerCase().contains("run")) {
                            PrefUtil.getInstance().put("currentStatus", "PRINTING");
                            this.isPrinting = true;
                        } else if (str4.toLowerCase().contains("hold")) {
                            PrefUtil.getInstance().put("currentStatus", "PAUSE");
                            this.isPrinting = true;
                        } else if (str4.toLowerCase().contains(NotificationCompat.CATEGORY_ALARM)) {
                            sendMsgToServer("$X\n".getBytes(), null);
                        }
                    }
                    if (i == split.length - 1 && str4.contains("SD:") && str4.contains(",")) {
                        String[] split2 = str4.split(",");
                        PrefUtil.getInstance().put("currentProgress", split2[0].split(":")[1]);
//                        PrefUtil.getInstance().put("engravingName", split2[1].split("/")[1].substring(0, r9[1].length() - 2));
                    }
                }
            }
        }
        if (str.toLowerCase().contains("ok")) {
            PrefUtil.getInstance().put("ReturnValue", str);
        }
    }

    private void dealespFileString(String str) {
        List<PrinterFiles> decodeFileListForESP = GcodeHelper.decodeFileListForESP(str);
        datas = decodeFileListForESP;
        dealFliesList(decodeFileListForESP);
    }

    public void setGetStatus(boolean z) {
        this.getStatus = z;
    }

    public void clearEspFileString() {
        espFileString = "";
    }

    public void getStatusThead() {
        new Thread(new Runnable() { // from class: makerbase.com.mkslaser.netty.NettyClient.4
            @Override // java.lang.Runnable
            public void run() {
                while (NettyClient.this.isConnect && NettyClient.this.getStatus) {
                    try {
                        if (!NettyClient.mIsBackground) {
                            NettyClient.this.sendMsgToServer("?".getBytes(), null);
                        }
                        Thread.sleep(1000L);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }).start();
    }

    public void getStatusTheadIsclosingLight(final int i) {
        new Thread(new Runnable() { // from class: makerbase.com.mkslaser.netty.NettyClient.5
            @Override // java.lang.Runnable
            public void run() {
                int i2 = 0;
                while (i2 < i) {
                    try {
                        Log.i("getStatuclosingLight", "run: " + i2);
                        if (!NettyClient.mIsBackground) {
                            NettyClient.this.sendMsgToServer("?".getBytes(), null);
                            i2++;
                        }
                        Thread.sleep(500L);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }).start();
    }
}
