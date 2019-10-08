package com.example.visiontranslation.helper;

import android.util.Size;

import com.example.visiontranslation.detector.text.Block;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

public class Helper {
    public static Block convertTextBlockToBlock(TextBlock block, Size size) {
        Block cBlock;

        block.getBoundingBox();

        return null;
    }

    public List<Block> convertTextBlocksToBlocks(List<TextBlock> blocks, Size size) {
        return null;
    }
}
