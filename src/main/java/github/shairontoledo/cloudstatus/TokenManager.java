package github.shairontoledo.cloudstatus;

import github.shairontoledo.cloudstatus.model.AccessToken;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.function.Consumer;


public class TokenManager {

    @Autowired
    DataObjectRepository<AccessToken> accessTokenRepository;

    public AccessToken fromToken(String token){
        for(AccessToken accessToken: accessTokenRepository.findBy("accesstoken:*")){
            if (token.equals(accessToken.getToken())){
                return accessToken;
            }
        }
        return null;
    }

    public AccessToken createToken(String identifier){
        AccessToken accessToken = new AccessToken();
        accessToken.setIdentification(identifier);
        accessToken.setToken((UUID.randomUUID().toString()+UUID.randomUUID().toString()).replaceAll("-",""));
        accessTokenRepository.save(accessToken);
        return accessToken;
    }

    public void listTokens(Consumer<AccessToken> consumer){
        for(AccessToken accessToken: accessTokenRepository.findBy("accesstoken:*")){
            consumer.accept(accessToken);
        }
    }

    public void deleteToken(String revokeId, Consumer<AccessToken> consumer){
        listTokens(accessToken -> {
            if (revokeId.equals(accessToken.getIdentification()) || revokeId.equals(accessToken.getToken())){
                accessTokenRepository.delete(accessToken.getId());
                consumer.accept(accessToken);
            }
        });
    }

}
