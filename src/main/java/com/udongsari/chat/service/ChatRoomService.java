package com.udongsari.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udongsari.chat.dto.ChatRoomDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Slf4j
@Data
@Service
public class ChatRoomService {
    private final ObjectMapper mapper;
    private Map<String, ChatRoomDto> chatRooms;

    @PostConstruct
    private void init() {
        chatRooms = new LinkedHashMap<>();
    }

    public List<ChatRoomDto> findAllRoom(){
        return new ArrayList<>(chatRooms.values());
    }

    public ChatRoomDto findRoomById(String roomId){
        return chatRooms.get(roomId);
    }

    public ChatRoomDto createRoom() {
        String roomId = UUID.randomUUID().toString();

        ChatRoomDto room = ChatRoomDto.builder()
                .roomId(roomId)
                .build();

        chatRooms.put(roomId, room);
        return room;
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }



}