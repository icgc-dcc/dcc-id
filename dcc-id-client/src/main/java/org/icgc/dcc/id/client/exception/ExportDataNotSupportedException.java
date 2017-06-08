package org.icgc.dcc.id.client.exception;

import com.sun.istack.NotNull;
import lombok.Data;

/**
 * Created by gguo on 6/8/17.
 */
public class ExportDataNotSupportedException extends RuntimeException {

    public ExportDataNotSupportedException(String type){
        super("Exporting " + type + " data is not supported!");
    }
}
