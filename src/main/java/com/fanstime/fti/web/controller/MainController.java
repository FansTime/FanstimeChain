package com.fanstime.fti.web.controller;

import com.fanstime.fti.config.RpcEnabledCondition;
import com.fanstime.fti.util.AppConst;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

    /**
     * Handles JSON-RPC requests at site root
     */
    @Conditional(RpcEnabledCondition.class)
    @RequestMapping(value = {AppConst.JSON_RPC_ALIAS_PATH}, method = RequestMethod.POST)
    public String jsonrpcAlias() {
        return "forward:" + AppConst.JSON_RPC_PATH;
    }
}
