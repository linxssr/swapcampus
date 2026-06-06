package com.campus.mapper;

import com.campus.model.entity.ChatMessage;
import com.campus.model.vo.ChatConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage chatMessage);

    List<ChatMessage> selectHistory(@Param("fromUid") Long fromUid, @Param("toUid") Long toUid);

    List<ChatConversationVO> selectConversations(@Param("uid") Long uid);

    int markRead(@Param("fromUid") Long fromUid, @Param("toUid") Long toUid);

    int markReadByMsgId(@Param("msgId") Long msgId, @Param("toUid") Long toUid);
}
