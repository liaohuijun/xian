package info.xiancloud.apifestoauth20.unit.scope;

import com.alibaba.fastjson.JSONObject;
import info.xiancloud.apifestoauth20.unit.OAuthService;
import info.xiancloud.core.*;
import info.xiancloud.core.message.UnitRequest;
import info.xiancloud.core.message.UnitResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.reactivex.Single;

/**
 * Created by Dube on 2018/5/14.
 */
public class RegisterScopeUnit implements Unit {

    @Override
    public Input getInput() {
        return new Input()
                .add("scope", String.class, "一次仅能添加一个scope", REQUIRED)
                .add("description", String.class, "自定义scope描述", REQUIRED)
                .add("cc_expires_in", Integer.class, "grant_type为client_credentials时access_token过期时间", REQUIRED)
                .add("pass_expires_in", Integer.class, "grant_type为password时access_token过期时间", REQUIRED)
                .add("refresh_expires_in", Integer.class, "grant_type为refresh_token时access_token过期时间，如果不填写，则使用pass_expires_in的值", NOT_REQUIRED);
    }

    @Override
    public String getName() {
        return "registerScope";
    }

    @Override
    public Group getGroup() {
        return OAuthService.singleton;
    }

    @Override
    public UnitMeta getMeta() {
        return UnitMeta.createWithDescription("注册 scope ")
                .setDocApi(true)
                .setSecure(false)
                .setSuccessfulUnitResponse(UnitResponse.createSuccess(new JSONObject() {{
                    put("status", "注册 scope 执行反馈");
                }})).addFailedUnitResponse(UnitResponse.create(new JSONObject() {{
                    put("error", "错误信息");
                }}));
    }

    @Override
    public void execute(UnitRequest msg, Handler<UnitResponse> handler) throws Exception {
        JSONObject json = new JSONObject() {{
            put("scope", msg.getString("scope"));
            put("description", msg.getString("description"));
            put("cc_expires_in", msg.get("cc_expires_in", Integer.class));
            put("pass_expires_in", msg.get("pass_expires_in", Integer.class));
            if (null != msg.get("refreshExpiresIn")) {
                put("refresh_expires_in", msg.get("refresh_expires_in", Integer.class));
            }
        }};
        String body = json.toJSONString(), uri = msg.getContext().getUri();
        ByteBuf byteBuffer = Unpooled.wrappedBuffer(body.getBytes());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri, byteBuffer);
        request.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);

        Single.just(OAuthService.getScopeService().registerScope(request)).subscribe(
                message -> handler.handle(UnitResponse.createSuccess(message)),
                exception -> handler.handle(UnitResponse.createException(exception))
        );
    }
}
