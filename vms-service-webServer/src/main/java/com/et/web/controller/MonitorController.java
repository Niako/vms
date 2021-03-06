package com.et.web.controller;

import com.et.web.entity.Combobox;
import com.et.web.entity.TreeNode;
import com.et.web.service.MonitorService;
import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by gaop on 2017/8/4.
 */
@Controller
@RequestMapping("/monitor")
public class MonitorController {

    Gson gson = new Gson();
    @Resource
    private MonitorService monitorService;

    @RequestMapping("/getGroupTree.do")
    @ResponseBody
    public List<TreeNode> getGroupTree(String id) {
        System.out.println("come on...." + "checked:" + " id:" + id);

        List<TreeNode> list = monitorService.getMonitorTree(id);

        for (TreeNode node : list) {
            System.out.println(node.getChildren().get(1).getText());
        }
        // String json= gson.toJson(list);
        return list;
    }
    @RequestMapping(value = "/getVehicleList.do", produces="text/html;charset=UTF-8")
    @ResponseBody
    public String getAllVehicle(){
        List<Combobox> box=monitorService.getAllVehicle();
           String json=gson.toJson(box);
           return json;
    }
}
