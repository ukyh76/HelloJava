package com.campus.service;

import com.campus.dao.UserDao;
import com.campus.face.FaceRecognitionService;
import com.campus.model.Role;
import com.campus.model.User;

public class UserService {
    private UserDao userDao;
    private FaceRecognitionService faceService ;

    public UserService(UserDao userDao,FaceRecognitionService faceRecognitionService) {
        this.userDao = userDao;
        this.faceService = faceRecognitionService;
    }
    //注册
    public boolean register(User user){
        if(user == null || user.getUsername() == null || user.getUsername().trim().isEmpty() ) {
            System.out.println("注册失败，注册信息不完整或用户名为空");
            return false;
        }


        User existingUser =  userDao.getUserByUsername(user.getUsername());
        if(existingUser != null) {
            System.out.println("注册失败，用户名["+user.getUsername()+"]已经存在");
            return false;
        }
        userDao.addUser(user);
        System.out.println("注册成功,用户名["+user.getUsername()+"]用户角色类型："+user.getRole().getDescription());
        return true;
    }

    //登录校验
    public User login(String username, String password){
        if(username == null || password == null){
            System.out.println("登录失败，用户名或密码不能为空。");
            return null;
        }
        User user = userDao.getUserByUsername(username);
        if(user == null){
            System.out.println("登录失败，系统未找到用户名为 "+username+" 账号。");
            return null;
        }
        if(!user.getPassword().equals(password)){
            System.out.println("登录失败，密码错误，请核对后重新登录");
            return null;
        }
        System.out.println("登录成功，欢迎回来"+username);
        return user;
    }

    //人脸识别登录
    public User loginByFace(String username, String loginFaceImage){
        if(username == null || loginFaceImage == null){
            System.out.println("人脸登录失败，用户名或者是人脸登录图片不能为空!");
            return null;
        }
        User user =  userDao.getUserByUsername(username);
        if(user == null)
            System.out.println("人脸登录失败,未找到对应用户!");
        String registedFace = user.getFaceImage();
        double similarity =  faceService.compare(user.getFaceImage(),loginFaceImage);
        System.out.println("人脸识别相似度:"+similarity);
        if(similarity >=80) {
            System.out.println(username + "欢迎您回来！");
            return user;
        }
        System.out.println("人脸识别失败!");
        return null;




    }

     //用户动态增删兴趣标签
    public void addUserTag(String username, String tag){    //?这里为什么传进来的参数不用 user，而要用 username 呢？
        if(username == null || tag == null || tag.trim().isEmpty()){
            System.out.println("操作失败，用户名或者是用户标签内容不能为空");
            return ;
        }
        User user =  userDao .getUserByUsername(username);
        if(user != null){
            user.addTag(tag);
            System.out.println("标签更新成功，已为用户 "+username+" 添加兴趣标签 "+ tag);
        }
        else {
            System.out.println("操作失败，未找到用户名为 "+username+"的用户。");
        }
    }

    public void printUserTag(String username)  //?这里为什么传进来的参数不用 user
    {
        User user = userDao.getUserByUsername(username);
        if (user != null) {
            System.out.println("【用户 [" + username + "] 的当前兴趣标签】: " + user.getTags());
        } else {
            System.out.println("【操作失败】未找到用户: " + username);
        }
    }


    //补充扩展方法：【权限校验辅助方法】
    /**
     *
     * 职责：统一校验当前操作用户是否具备指定的角色权限
     * @param user 当前操作用户
     * @param requiredRole 所需的目标角色
     * @return 如果匹配返回 true，否则返回 false 并打印提示
     */
    public boolean checkUserPermission(User user, Role requiredRole) {
        if (user == null) {
            System.out.println("【权限拦截】校验失败：当前处于未登录状态！");
            return false;
        }

        if (user.getRole() != requiredRole) {
            System.out.println("【权限拦截】操作拒绝：用户 [" + user.getUsername() +
                    "] 的角色是 [" + user.getRole().getDescription() +
                    "]，无权执行此操作（需要 [" + requiredRole.getDescription() + "] 权限）！");
            return false;
        }

        return true;
    }

}
