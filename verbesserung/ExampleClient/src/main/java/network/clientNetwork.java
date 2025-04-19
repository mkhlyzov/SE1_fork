package network;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.ResponseEnvelope;
import messagesbase.messagesfromclient.ERequestState;
import messagesbase.messagesfromclient.PlayerRegistration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromclient.PlayerMove;


public class clientNetwork {

    // === Attribute ===
    private final String baseURL;
    private final String gameId;
    private UniquePlayerIdentifier playerId;

    

    // === Konstruktor ===
    public clientNetwork(String baseURL, String gameId) {
        this.baseURL = baseURL;
        this.gameId = gameId;
    }
    
    public GameState getGameState() {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseURL + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();

        Mono<ResponseEnvelope<GameState>> webAccess = webClient
                .method(HttpMethod.GET)
                .uri("/" + gameId + "/states/" + playerId.getUniquePlayerID())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});

        ResponseEnvelope<GameState> result = webAccess.block();

        if (result.getState() == ERequestState.Error) {
            System.err.println("❌ Fehler beim Abrufen des Spielstatus: " + result.getExceptionMessage());
            return null;
        }

        return result.getData().get();
    }
    // === Registrierung implementiert ===
    public void registerPlayer(String studentUAccount) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseURL + "/games") // ❗ port NICHT nochmal anhängen
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();

        PlayerRegistration playerReg = new PlayerRegistration("Dmytro", "Kostariev", studentUAccount);

        Mono<ResponseEnvelope<UniquePlayerIdentifier>> webAccess = webClient
                .method(HttpMethod.POST)
                .uri("/" + gameId + "/players")
                .body(BodyInserters.fromValue(playerReg))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

        ResponseEnvelope<UniquePlayerIdentifier> result = webAccess.block();

        if (result.getState() == ERequestState.Error) {
            System.err.println("❌ Fehler bei der Registrierung: " + result.getExceptionMessage());
            return;
        }

        playerId = result.getData().get();

        // ✅ Exakt dieser Output wie gewünscht
        System.out.println(playerId);
        System.out.println("🧓 \"Classic\" approach, but still possible " + playerId + " 🎉");
    }

    // === Platzhalter-Methoden für später ===
    public void sendHalfMap(Object halfMapData) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseURL + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();

        System.out.println("📤 Sende HalfMap an den Server...");

        Mono<ResponseEnvelope<Object>> webAccess = webClient
                .method(HttpMethod.POST)
                .uri("/" + gameId + "/halfmaps")
                .body(BodyInserters.fromValue(halfMapData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<Object>>() {});

        ResponseEnvelope<Object> result = webAccess.block();

        if (result == null) {
            System.err.println("🚫 Keine Antwort vom Server auf HalfMap-Sendung erhalten.");
            return;
        }

        if (result.getState() == ERequestState.Error) {
            System.err.println("❌ Fehler beim Senden der HalfMap: " + result.getExceptionMessage());
        } else {
            System.out.println("✅ HalfMap erfolgreich an Server übermittelt.");
        }
    }


    public void getGameStatus() {
        System.out.println("📥 Spielstatus wird abgefragt...");
    }

    public void sendMove(PlayerMove move) {
        WebClient webClient = WebClient.builder()
            .baseUrl(baseURL + "/games")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .build();

        Mono<ResponseEnvelope<Object>> webAccess = webClient
            .method(HttpMethod.POST)
            .uri("/" + gameId + "/moves")
            .body(BodyInserters.fromValue(move))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<Object>>() {});

        ResponseEnvelope<Object> result = webAccess.block();

        if (result.getState() == ERequestState.Error) {
            System.err.println("❌ Fehler beim Senden des Zuges: " + result.getExceptionMessage());
        } else {
            System.out.println("✅ Zug erfolgreich gesendet!");
        }
    }

    // === Getter ===
    public String getBaseURL() {
        return baseURL;
    }

    public String getGameId() {
        return gameId;
    }

    public UniquePlayerIdentifier getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UniquePlayerIdentifier playerId) {
        this.playerId = playerId;
    }
}
