package cn.lqs.vget.core.hls;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalTsSegFile {
    private int order;
    private String path;
}
