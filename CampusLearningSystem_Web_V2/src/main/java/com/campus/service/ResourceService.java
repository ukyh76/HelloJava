package com.campus.service;

import com.campus.ai.AiAssistant;
import com.campus.ai.LlmAssistant;
import com.campus.dao.ResourceDao;
import com.campus.dao.impl.InMemoryResourceDao;
import com.campus.model.LearningResource;
import com.campus.model.Role;
import com.campus.model.User;
import com.campus.index.InvertedIndex;

import java.util.*;

public class ResourceService{
    private ResourceDao resourceDao = new InMemoryResourceDao();
    private InvertedIndex invertedIndex  = new InvertedIndex();
    private AiAssistant assistant=  new LlmAssistant();
    private AutoTagService autoTagService =  new AutoTagService(assistant);

    /**
     * 【核心功能 1：添加学习资料（含权限拦截与索引联动）】
     * 业务流程：
     * 1. 权限校验：操作人必须为 TEACHER。
     * 2. 持久化存储：存入内存 DAO 容器中。
     * 3. 索引联动：送入倒排索引引擎建立关键词映射。
     */
    public boolean addResource(User operator , LearningResource resource){
        if(operator == null || operator.getRole() != Role.TEACHER) {
            System.out.println("权限拦截,操作被拒绝：只有教师或管理员才能录入学习资料");
            return false;
        }
        if(resource == null)
            System.out.println("添加资料失败，资料对象不能为空");
    //增加 AI 自动生成标签功能,调试的时候为节省 AI token，可以暂时关闭此功能
//        List<String> tags = autoTagService.generateTags(resource.getTitle(),resource.getCategory());
//        for (String tag:tags){
//            resource.addTag(tag);
//        }

        resourceDao.addResource(resource);   //2. 持久化存储：存入内存 DAO 容器中。
        // 步骤 3：核心联动 —— 自动构建倒排索引
        invertedIndex.addResource(resource);
        System.out.println("资料录入成功，教师 "+operator.getUsername()+"  录入资料:"+resource.getTitle());
        return true;

    }
//    private void buildInvertedIndexorResource(LearningResource resource){
//        String[] words = resource.getTitle().split("\\s+");
//        for(String word :words){
//            invertedIdex.computeIfAbsent(word,k->new HashSet<>()).add(resource.getId());
//        }
//        for(String tag:resource.getTags()){
//            invertedIdex.computeIfAbsent(tag,k->new HashSet<>()).add(resource.getId());
//        }
//    }
    public List<LearningResource> searchByKeyword(String keyword){
        if(keyword == null || keyword.trim().isEmpty()){
            System.out.println("检索提示输入的关键词为空");
            return new ArrayList<>();
        }

        Set<Integer> matchedIds = invertedIndex.search(keyword);
        List<LearningResource> result = new ArrayList<>();
        for (int id:matchedIds) {
            LearningResource resource = resourceDao.getResourceByID(id);
            if(resource !=null)
                result.add(resource);
        }
        System.out.println("检索完成, 关键词 "+keyword +"  共匹配到 "+result.size()+" 条学习资源!");
        return  result;
    }

    public List<LearningResource> getAllResource(){
        return resourceDao.getAllResources();
    }
    public LearningResource getResourceById(int id){
        return resourceDao.getResourceByID(id);
    }

    public boolean deleteResource(User operator, int id){
        if(operator == null || operator.getRole()  != Role.TEACHER){
            System.out.println("权限拦截操作失败,"+operator.getUsername() +" 没有权限删除学习资料.");
            return false;
        }

        LearningResource resource = resourceDao.getResourceByID(id);
        if(resource != null){
            resourceDao.deleteResource(id);
            //核心同步联动,同步从倒排索引中剔除该资料的关键词映射，防止脏数据
            invertedIndex.removeResouce(resource);

            System.out.println("删除成功，已移除学习资料:"+resource.getTitle());
            return true;
        }
        else {
            System.out.println("删除失败，未找到 ID 为 "+id +" 的资料!");
            return false;
        }
    }

}


