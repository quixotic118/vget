package cn.lqs.vget.core.hls;

import java.util.Objects;

public record EncryptInfo(String encryptedMethod, String encryptKeyUri, String encryptKeyIV) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptInfo that = (EncryptInfo) o;
        return Objects.equals(encryptedMethod, that.encryptedMethod) && Objects.equals(encryptKeyUri, that.encryptKeyUri) && Objects.equals(encryptKeyIV, that.encryptKeyIV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptedMethod, encryptKeyUri, encryptKeyIV);
    }
}
