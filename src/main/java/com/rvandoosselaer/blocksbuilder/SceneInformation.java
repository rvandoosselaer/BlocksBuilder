package com.rvandoosselaer.blocksbuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A bean holding some information about the scene. Filename, last save time etc.
 * @author: rvandoosselaer
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneInformation {

    private String filename;
    private long saveTimestamp;

    public void clear() {
        filename = null;
        saveTimestamp = -1;
    }

    public void save(String filename) {
        this.filename = filename;
        this.saveTimestamp = System.currentTimeMillis();
    }

}
