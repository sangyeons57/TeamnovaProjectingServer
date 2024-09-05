package com.example.teamnovapersonalprojectprojecting.util;

import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class ServerLibraryTest extends ServerConnectManager {
    public ServerLibraryTest(String path) {
        super(path);
    }

    public static void NettyTest() {
        EventLoopGroup group = new NioEventLoopGroup();
        Log("NettyTest");
        new Thread(() -> {
            try {
                Log("Thread Starat");
                Bootstrap bootstrap = new Bootstrap();
                Log("create bootstarp");
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpRequestDecoder());
                                pipeline.addLast(new HttpRequestEncoder());
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                pipeline.addLast(new SimpleChannelInboundHandler<HttpObject>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                                        if (msg instanceof HttpResponse) {
                                            HttpResponse response = (HttpResponse) msg;
                                            Log("Response: " + response);
                                        }
                                        if (msg instanceof HttpContent) {
                                            HttpContent content = (HttpContent) msg;
                                            Log("Content: " + content.content().toString(CharsetUtil.UTF_8));
                                        }
                                    }
                                });

                            }
                        });
                Log("try");
                //HTTP요청 TCP연결 생성
                ChannelFuture future = bootstrap.connect("3-34-42-15.ap-northeast-2.compute.amazonaws.com", 80).sync();
                Log("connect");

                DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
                        Unpooled.copiedBuffer("{\"message\":\"Hello, PHP Server!\"}", CharsetUtil.UTF_8));
                Log("before headers");

                request.headers().set(HttpHeaders.Names.HOST, SocketConnection.SERVER_ADDRESS + Path.TEST.getPath("NettyTest.php"));
                request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
                request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

                Log("futture");
                future.channel().writeAndFlush(request).addListener(ChannelFutureListener.CLOSE);

                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void AsyncHttpClientTest() {
        // Create an instance of AsyncHttpClient
        AsyncHttpClient client = new AsyncHttpClient();
        String TAG = "AsyncHttpClientTest";
        RequestParams params = new RequestParams();
        params.put("param1", "value1");
        params.put("param2", "value22");

        // Perform a GET request to the specified URL
        client.post(SocketConnection.SERVER_ADDRESS + Path.TEST.getPath("AsyncHttpClientTest.php"), params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // Called before the request is started
                Log.d(TAG, "Request started");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Called when response HTTP status is "200 OK"
                String response = new String(responseBody);
                Log.d(TAG, "Request success: " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Called when response HTTP status is "4XX" (client error) or "5XX" (server error)
                Log.e(TAG, "Request failure: " + error.toString());
                if (responseBody != null) {
                    String response = new String(responseBody);
                    Log.e(TAG, "Response: " + response);
                }
            }

            @Override
            public void onRetry(int retryNo) {
                // Called when request is retried
                Log.d(TAG, "Request retry: " + retryNo);
            }

            @Override
            public void onFinish() {
                // Called after the request is finished
                Log.d(TAG, "Request finished");
            }

        });
    }

    public static void UndertowTest() {
        //underTow clientConnectionBuilder실패
        // undertow 웹서버로 만들어진 라이브러리여서 그런지 자료가 부작하고 다른 라이브러리에 비해 잘 안되는거 같음
    }
}
