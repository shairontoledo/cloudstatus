package github.shairontoledo.cloudstatus;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import github.shairontoledo.cloudstatus.model.AccessToken;
import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import github.shairontoledo.cloudstatus.task.Seed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.ArrayList;
import java.util.List;

public class CommandLineTools implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    @Autowired
    DataObjectRepository<AccessToken> accessTokenRepository;

    @Autowired
    Seed seed;

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "--list-tokens", "-l" }, description = "List Tokens")
    private boolean listToken = false;

    @Parameter(names = {"--create-token","-c"}, description = "Create token")
    private String createTokenId;

    @Parameter(names = {"--revoke-token","-r"}, description = "Revoke token")
    private String revokeId;

    @Parameter(names = {"--seed","-s"}, description = "Load a seed file")
    private String seedFile;

    @Autowired
    private TokenManager tokenManager;

    @Override
    public void run(String... args) throws Exception {

        JCommander commander = new JCommander(this, args);

        if (listToken){
            logger.info(String.format("*** Listing tokens:"));
            tokenManager.listTokens( accessToken ->
                logger.info(String.format("*** Token: '%s', identification '%s'\n",
                        accessToken.getToken(), accessToken.getIdentification())));

            System.exit(0);
        }
        if (revokeId != null){
            tokenManager.deleteToken(revokeId, accessToken -> {
                logger.info(String.format("*** Revoked Token: '%s', identification '%s'\n",
                        accessToken.getToken(), accessToken.getIdentification()));});

            System.exit(0);
        }
        if(createTokenId != null){
            AccessToken accessToken = tokenManager.createToken(createTokenId);
            logger.info(String.format("\n*** New token '%s' has been created for '%s'", accessToken.getToken(), accessToken.getIdentification()));
            System.exit(0);
        }

        if(seedFile != null){
            logger.info("Loading seed file");
            seed.seed(seedFile);
            System.exit(0);
        }

    }
}
