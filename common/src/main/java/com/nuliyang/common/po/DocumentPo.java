package com.nuliyang.common.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPo {

    private Long id;

    private Long resourceId;

    private String metaData;

    private String textWords;
}
