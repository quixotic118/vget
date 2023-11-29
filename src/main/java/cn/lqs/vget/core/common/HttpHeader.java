package cn.lqs.vget.core.common;

import java.util.ArrayList;
import java.util.Arrays;

public record HttpHeader(String name, String value, String... values) {


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ArrayList<HttpHeader> headers;
        private Builder (){
            this.headers = new ArrayList<>();
        }

        public Builder referer(String referer) {
            headers.add(new HttpHeader(HttpHeaderNVs.Referer, referer));
            return this;
        }

        public Builder userAgent(String userAgent) {
            headers.add(new HttpHeader(HttpHeaderNVs.UserAgent, userAgent));
            return this;
        }

        public Builder cookie(String cookie) {
            headers.add(new HttpHeader(HttpHeaderNVs.Cookie, cookie));
            return this;
        }

        public Builder header(String name, String... values) {
            headers.add(new HttpHeader(name, values[0], Arrays.stream(values).skip(1).toArray(String[]::new)));
            return this;
        }

        public Builder header(String name, String value, String... values) {
            headers.add(new HttpHeader(name, value, values));
            return this;
        }

        public HttpHeader[] build() {
            return headers.toArray(new HttpHeader[0]);
        }
    }

}
