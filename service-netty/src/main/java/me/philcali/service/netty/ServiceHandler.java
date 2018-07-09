package me.philcali.service.netty;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.request.Request;
import me.philcali.service.binding.response.IResponse;

public class ServiceHandler extends SimpleChannelInboundHandler<Object> {
    private static final Pattern ROUTE_PATH = Pattern.compile("\\({[^\\}]+\\})");
    private final RequestRouter router;

    public ServiceHandler(final RequestRouter router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object req) throws Exception {
        final DefaultFullHttpRequest request = (DefaultFullHttpRequest) req;
        final DefaultFullHttpResponse response = router.compose(this::translateRequest)
                .andThen(this::translateReponse)
                .apply(request);
        ctx.writeAndFlush(response);
    }

    private DefaultFullHttpResponse translateReponse(final IResponse response) {
        final DefaultFullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.getStatusCode()));
        response.getHeaders().forEach((key, value) -> {
            res.headers().add(key, value);
        });
        Optional.ofNullable(response.getBody())
                .filter(body -> !body.isEmpty())
                .ifPresent(body -> res.replace(Unpooled.copiedBuffer(body.getBytes(StandardCharsets.UTF_8))));
        return res;
    }

    private IRequest translateRequest(final DefaultFullHttpRequest req) {
        final Request request = new Request();
        final String fullPath = req.uri().split("\\?")[0];
        request.setPath(fullPath);
        request.setHttpMethod(req.method().name());
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
                final Pattern pathPattern = Pattern.compile(matcher.replaceAll("\\(\\[^/\\]+\\)/?"));
                final Matcher patternMatcher = pathPattern.matcher(fullPath);
                if (patternMatcher.find() && matcher.groupCount() == patternMatcher.groupCount()) {
                    for (int groupIndex = 1; groupIndex <= patternMatcher.groupCount(); groupIndex++) {
                        pathParameters.put(matcher.group(groupIndex), patternMatcher.group(groupIndex));
                    }
                    break;
                }
            }
        }
    }
}
