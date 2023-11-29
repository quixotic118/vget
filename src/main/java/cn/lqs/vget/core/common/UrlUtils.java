package cn.lqs.vget.core.common;

import cn.lqs.vget.core.exceptions.HttpUrlHrefException;

public class UrlUtils {

    /**
     * 模拟浏览器对 url 拼接的处理
     * @param base 原URL
     * @param href 在原URL下要访问的URL
     * @return 最终URL地址
     */
    public static String mockHref(String base, String href) throws HttpUrlHrefException {
        if (!base.startsWith("http")) {
            throw new HttpUrlHrefException("base url should specify a http/https protocol.");
        }
        base = base.split("\\?")[0];
        if (href.startsWith("http")) {
            return href;
        } else if (href.startsWith("//")) {
            int index = base.indexOf(':');
            if (index == -1) {
                throw new HttpUrlHrefException("Can't extract scheme from base url " + base);
            }
            return base.substring(0, index + 1) + href;
        } else if (href.startsWith("/")) {
            int i = base.indexOf("//");
            if (i != -1) {
                int i1 = base.substring(i + 2).indexOf('/');
                return i1 == -1 ? base + href : base.substring(0, i1 + i + 2) + href;
            }
            throw new HttpUrlHrefException("Can't compose the base url [" + base + "] with [" + href + "].");
        } else {
            int i = base.indexOf("//");
            if (i != -1) {
                int i1 = base.lastIndexOf('/');
                if (i1 == -1) {
                    return base + "/" + href;
                }
                return base.substring(0, i1 + 1) + href;
            }
            throw new HttpUrlHrefException("Can't compose the base url [" + base + "] with [" + href + "].");
        }
    }
}
