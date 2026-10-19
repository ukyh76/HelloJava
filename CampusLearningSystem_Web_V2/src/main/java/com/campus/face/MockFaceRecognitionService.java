package com.campus.face;

import com.sun.source.tree.BreakTree;

public class MockFaceRecognitionService implements FaceRecognitionService{

    @Override
    public double compare(String registeredImage, String loginImage) {
        //先通过比较两个图片文件的名字：
        //1. 如果相等，返回 98%，表示对比成功
        //2. 如果不相等，返回 18%，表示对比不成功
        //3. 如果有一个为空，返回 0
        //
        //这只是模拟一下，并没有真正做图片识别，后一步再做。
        if(registeredImage == null || loginImage == null)
            return 0;

        if(registeredImage.trim().equalsIgnoreCase(loginImage.trim()))
            return 96.0;
        return 18.0;
    }
}
