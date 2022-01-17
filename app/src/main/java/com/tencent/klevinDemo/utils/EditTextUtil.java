package com.tencent.klevinDemo.utils;

import android.widget.EditText;

public class EditTextUtil {
    /**
     * @return 当Edt内容为空时, 返回其Hint值.
     */
    public static String getText(EditText edt) {
        String text = edt.getText().toString();
        return text.isEmpty() ? edt.getHint().toString() : text;
    }
}
