package me.philcali.service.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import me.philcali.service.binding.RequestRouter;

public class ServiceInitializer extends ChannelInitializer<SocketChannel> {
    private final RequestRouter router;

    public ServiceInitializer(final RequestRouter router) {
        this.router = router;
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new ServiceHandler(router));
    }
}
