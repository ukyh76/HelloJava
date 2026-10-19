package com.campus.face;

public interface FaceRecognitionService {
    double compare(String registedImage, String loginImage);
}
