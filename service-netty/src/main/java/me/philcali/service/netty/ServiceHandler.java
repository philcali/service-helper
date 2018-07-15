package me.philcali.service.netty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.request.Request;
import me.philcali.service.binding.response.IResponse;

public class ServiceHandler extends SimpleChannelInboundHandler<Object> {
    private static final Pattern ROUTE_PATH = Pattern.compile("\\{([^\\}]+)\\}");
    private final RequestRouter router;

    private HttpRequest request;
    private StringBuilder buffer = new StringBuilder();

    public ServiceHandler(final RequestRouter router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object req) throws Exception {
        if (req instanceof HttpRequest) {
            request = (HttpRequest) req;
        } else if (req instanceof HttpContent) {
            final HttpContent content = (HttpContent) req;
            buffer.append(new String(content.content().array(), StandardCharsets.UTF_8));
        }
        if (req instanceof LastHttpContent){
            final DefaultFullHttpResponse response = router
                    .compose(this::translateRequest)
                    .andThen(this::translateReponse)
                    .apply(request);
            ctx.write(response);
            if (!HttpUtil.isKeepAlive(request)) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private DefaultFullHttpResponse translateReponse(final IResponse response) {
        final DefaultFullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.getStatusCode()));
        Optional.ofNullable(response.getHeaders()).ifPresent(headers -> headers.forEach((key, value) -> {
            res.headers().add(key, value);
        }));
        String resBody = response.getBody();
        if (Objects.nonNull(response.getException())) {
            final StringWriter writer = new StringWriter();
            response.getException().printStackTrace(new PrintWriter(writer));
            resBody = writer.toString();
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        }
        if (HttpUtil.isKeepAlive(request)) {
            res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return Optional.ofNullable(resBody)
                .filter(body -> !body.isEmpty())
                .map(body -> {
                    final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    final DefaultFullHttpResponse replaced = (DefaultFullHttpResponse) res.replace(Unpooled.copiedBuffer(bytes));
                    replaced.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                    return replaced;
                })
                .orElse(res);
    }

    private IRequest translateRequest(final HttpRequest req) {
        final Request request = new Request();
        final String fullPath = req.uri().split("\\?")[0];
        request.setPath(fullPath);
        request.setHttpMethod(req.method().name());
        request.setBody(buffer.toString());
        setRequestPathInformation(request, fullPath);
        final Map<String, String> headers = new HashMap<>();
        req.headers().forEach(entry -> {
            headers.put(entry.getKey(), entry.getValue());
        });
        request.setHeaders(headers);
        QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
        request.setQueryStringParameters(queryDecoder.parameters().entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> {
                    final StringJoiner joiner = new StringJoiner(";");
                    e.getValue().forEach(joiner::add);
                    return joiner.toString();
                })));
        return request;
    }

    private void setRequestPathInformation(final Request request, final String fullPath) {
        final Map<String, String> pathParameters = new HashMap<>();
        request.setResource(fullPath);
        request.setPathParameters(pathParameters);
        for (final ResourceMethod method : router.getResourceMethods()) {
            final Matcher matcher = ROUTE_PATH.matcher(method.getPatternPath());
            if (matcher.find()) {
                final Pattern pathPattern = Pattern.compile(matcher.replaceAll("\\(\\[^/\\]+\\)/?") + "$");
                final Matcher patternMatcher = pathPattern.matcher(fullPath);
                if (patternMatcher.find() && matcher.groupCount() == patternMatcher.groupCount()) {
                    request.setResource(method.getPatternPath());
                    matcher.reset(method.getPatternPath()).find();
                    for (int groupIndex = 1; groupIndex <= patternMatcher.groupCount(); groupIndex++) {
                        pathParameters.put(matcher.group(groupIndex), patternMatcher.group(groupIndex));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer(writer.toString(), CharsetUtil.UTF_8));
        ctx.writeAndFlush(response);
        super.exceptionCaught(ctx, cause);
    }
}
