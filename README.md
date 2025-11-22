# 📋 미니멀 웹서버

---

## 📝 프로젝트 개요

이 프로젝트는 **웹서버**를 구현하는 것을 목표로 합니다.

- 요청 라인/헤더/바디를 처리하는 HTTP 파서와 응답 빌더 직접 구현
- 라우터로 정적/동적 요청 분기 (확장자/패턴 기반)
- 역프록시 핸들러로 WAS와 연동 (헤더 정리, Content-Length 설정)

---

## 🛠 기술 스택

![Java](https://img.shields.io/badge/java-005F0F?style=for-the-badge&logo=java&logoColor=white)

---

## 📺 화면

- **정적파일 요청**  

<img width="1920" height="1080" alt="정적파일" src="https://github.com/user-attachments/assets/18b77b6d-595d-472d-99ee-a82c275eb1d2" />

- **HTML 요청**    

<img width="1920" height="1080" alt="was html" src="https://github.com/user-attachments/assets/41652736-7bcf-48e2-b587-489ccf8a354c" />


- **API GET 요청**  

<img width="1920" height="1080" alt="api get" src="https://github.com/user-attachments/assets/0036b631-fca9-4363-be6c-83aefbe15b56" />


- **API POST 요청** 

https://github.com/user-attachments/assets/70d2a4f8-383e-48c5-ba47-25869610ce1b


---

## 💡 주안점

### 1. 요청 라인/헤더/바디를 처리하는 HTTP 파서와 응답 빌더 직접 구현

프로젝트 초반에는 채팅방 리스트를 단순히 문자열로 이어붙여 전송했지만,  
데이터가 많아지고 구조가 복잡해질수록 파싱 로직이 길어지고 유지보수가 어려워지는 문제가 있었습니다.  
이를 해결하기 위해 **Room 객체 자체를 직렬화해 전송**하고, 클라이언트에서 역직렬화를 통해 동일한 객체 구조로 재사용할 수 있도록 개선했습니다.

<details>
  <summary>코드 보기 (펼치기/접기)</summary>

Room Class

    /**
     * Room Dto
     */
    public class Room implements Serializable {
      private static final long serialVersionUID = 1L;
      ...
    }

 객체화 메소드
  
      private byte[] serializeRooms(List<Room> rooms) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(rooms);
        objectOutputStream.flush();
      
        return byteArrayOutputStream.toByteArray();
      }

데이터 보내기
  
      dataOutputStream.writeUTF("ROOM_LIST");
      dataOutputStream.writeInt(roomsByteArray.length);
      dataOutputStream.write(roomsByteArray);
  

  

  [RoomListCommand 전체 코드](https://github.com/rooluDev/chatting-java/blob/main/server/src/command/RoomListCommand.java)
  </details>

---

