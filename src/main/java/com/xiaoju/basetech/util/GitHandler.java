package com.xiaoju.basetech.util;

/**
 * @description:
 * @author: gaoweiwei_v
 * @time: 2019/6/20 4:28 PM
 */

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class GitHandler {
    static final Logger logger = LoggerFactory.getLogger(GitHandler.class);

    @Value(value = "${gitlab.username}")
    private String username;

    @Value(value = "${gitlab.password}")
    private String password;

    //    private String private_key = Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toString();
    private String private_key = Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa_liuh50").toString();

//    private String private_key = "-----BEGIN RSA PRIVATE KEY-----\n" +
//            "MIIJKQIBAAKCAgEAwBOWCsgPvIYRZ+6vqDF0YqY2NWTawbwSG1yRjDUZF0m7BOWI\n" +
//            "IIFLXZxcIlGJPVedfeV1587NlHXCQZeGjC2mP30WKCqIl3b9c1sPdIXmcqvQoZ2g\n" +
//            "28VwDw38eHKSssi6lkwZ9H27rNgQdaDaftkyAZq5NgiyYFPXTExJ+LvZrCNKtY7d\n" +
//            "Y7CO63EFPGXcyHni5QqFGrU+O0Y8jvTKHMooa3EKo1ApqLvwnOk8Ssxxa9V0frWt\n" +
//            "oCskPtgJKfqaUCsuIbwsZM/8oQ00aPryFytyfVJ8ezYx509sWvhI2bIUQCNmFAr1\n" +
//            "5Y1o+DeSJ23yUUfaKLNRKTni84wl3e99kGgvIRASF7PdbJ0b4c5SfxM8zHCoaRfa\n" +
//            "jKd+XRcGsXXjLxv+AeqSuIkAg1ABv5lzxBXY4jGLSk5Kt2P0RVmgdmGwgM58/Cz6\n" +
//            "o9R2teyZH89GbdiAiEz4F1wI8Nqf2qoSF0r1XEIGNY+O+Ik+LnLIpNQtkCxVTJg9\n" +
//            "dyhcAfRChcrSA+Ycj2KZh1v4cBmUzS9jFYQxLfek5gK0+D2sNxX5jVdqBIEqAC+Y\n" +
//            "bR9aVb7bcUjzk9soYlSSrHHZguB8yX2oSJH/J2hOWJ3QQ1Y8LBGaVkL3UjvSu0Gx\n" +
//            "S8fegy/32c38Q8f5VhXqXKc9GOKYtl8Ii/V6E6A8A4ui1xH12KLMTU6EiRsCAwEA\n" +
//            "AQKCAgBOkDRQ2J/HJrc74Z8rln7aUs3dbRElKP3m/yfxR06p/dhnGlNhqEDhWzE8\n" +
//            "QEl4M7wRYKenVykzTi9UkNvRvmI/mbRUXYFdIqhsZJSfvp0RzkEZudWvzsQVBE1+\n" +
//            "Hq7uPbhD/YCoRj1bZ0zkMBTuyXvwuA6FkW0UhXaMmK+w83UHObA3LuZS/kaW+rV/\n" +
//            "jNKPlXs+39ytnfCEYigm+O0qdW2u9J+7SPChOa4jICsvFQxXZQmvQjRwTPUYV1j4\n" +
//            "OK6hl+fYmJU37vKQFUyYm/wlifncVL7hzrr+t7fERcXWdUZfLZt1Gz6U1BBZZrF7\n" +
//            "OzdgE5miCCZsTGcN4wciDfE3/cU4dTazAAHA4TvXz+vN+GKzMPST5l9s6T2qSTfF\n" +
//            "5O9WhqTaONLCsffPneZeI6hf/D4mAWhvslbIYhDK8g58jNn3sYmdkYjNqJ2VyMYp\n" +
//            "6w3qsENH9yjcj3oo+cFK3ZqPW6ZgQc/wlB/akqtN4250mmID/aPTjwKonQkU/cGA\n" +
//            "xc1eVzdtn95PO10S2I0S0LpzYpptfphsO5afcFfcSPWzCsX1Alp8cs6LpHibCggl\n" +
//            "krOeRlQCxsp+4Hyhd5TUErqEZFMi+2hZ/Y4rNL5YFNd8zHEdu3uiuYLOY430DbG6\n" +
//            "xgRkXSwfVKDNLtKxHJCLH2bnl0IHq0Hbe8NZPnelYj5nH5qEoQKCAQEA6GMWPtc1\n" +
//            "MdZaDzTBwqlF/mxuIhPZrG4jc0VR+cHgodjPdrlJeFjSRJqL5SJvB1nfmSauIIEL\n" +
//            "9awUnIh36pYHuhcuRWalJ1dunX9u2Xx4NNlcMdQvKBj+RzfF4p0glrrjdXRsH2E3\n" +
//            "I26pP+uof47o0Y4AnuFFZ5q/VyEyXUZMJ8xFen3fpEBmwgevkr5king/Lw4j4i3R\n" +
//            "W1+vxPQUnsHv5IhzxplzmY5gap3uimT2j8pzoIajq4MsVnC84/wuJQefuodlAI8i\n" +
//            "v9kxW76SkOdbXeQwaLAC3MxrImkAPdc8JeKbe55ZjLilKZ93aPElea/mElZwTHYj\n" +
//            "Zj2jVN/p6/G/uQKCAQEA05fuPE7/tqJUAaBxd04ZmPPebzPdFx224+51O7ZjhpXh\n" +
//            "R599ChlNtQg8QtHD8k2Ori6fUq9N8kDO3HFM/hRf8tN8eC6jRKuffb5Y9C+h4heE\n" +
//            "ensGlot1Awtwc5foakGAaxSGJceBY09GtwOk5qKHClSIBGD9+ahG4l0avsIrymgA\n" +
//            "zD3QnUMfRcnvz75NuIQeqHGtSeQLo0tH9Aqvl3hQIAayC1aVQYe8sN1r35Ho0S4i\n" +
//            "GCOWhu3mCXsU/U/45ZHxURv90I5XllpMyq6J4UKAdYRcxFy37Nf+sPZZcPsolc1+\n" +
//            "cvMBygyn/ODM+MjGQBrxn+mstgIUZV1yC0jqZ4ExcwKCAQEAziTX3vbZYVRcn5CV\n" +
//            "MTEnvgJB0Ej7l1I8cEzV41BxDCFgKFlFK1TfcawG3UQmacb36su9O2g+S7hSsGj+\n" +
//            "t0JPqGxXx4i+iJ+uqFga5SLdH0SR3EH1cwR4zIRVLilkd1Fh5RdZspxyZSQNT36l\n" +
//            "AkKrlPj64/XBs546jfgMzWFGvScfmwF+Gh5nCUf4H5x1Y8A3jG5mtoUeaof/TB+Z\n" +
//            "CxvOylOhIFOPkHAUZ3+h03KspfFZkNTe6455DlUDutpBtOeGcdkZZ92RXc41UsVD\n" +
//            "DvesvSVnK/a3E1bi0quCY1lDKfhTJN3bYlGJcdrb+mOwcrnuxeWXyLwjutaCxQ+8\n" +
//            "WmkhQQKCAQBbIKbQwHEu9fBaFwBnEnLBxd25ZMvGRdzbg3pn8iAg+XHhh9WxQL6P\n" +
//            "xfE2EeNi+nYFnDyvvqJCQBkXJZC0KJA+I0kK9YMJcySoYg8rc0wz9E7w9QUcVwlY\n" +
//            "+1c94WGOSZGGDHvNz4NxAtaAwYgGcF7aWxeNUzxuhkOo/9Ih77wuWuZlEAZFhH/2\n" +
//            "bYbjoKEWY4hUoUixdw5wEF73wpZnFMDlWhzZ9zsm0s+4GFR6H/v2AvlaDhGpINIe\n" +
//            "qSycABJAxL76YKTKalNchJ1n+Q2Wvy5V7mCJjSkFTuz4x9AzcWfERJE97lyJ6TN8\n" +
//            "HFEKoGWJfFEtvtTMOk8QbNNN+uRBGYnzAoIBAQCXcE/ycha8xMK8TpnyUxyzI+q3\n" +
//            "AeXp6Xs2UUbL9GD6ljRvZMa9U1XdnfI/xvi9MnEiIXdJaCXcYjKhBMxxIznEpkDa\n" +
//            "jH9OH85gm4rCEuZfbo70heEVlsB+rU2zBwW8KBNeoSXMUId++rju/hL+PiPWlELT\n" +
//            "7n8HfYIUNVtMloUkoHFx8+s55SJnOc2EQWfVVkGly7f8IiXl6cdWCyO/B6gxZWop\n" +
//            "/henBvcH/Aodf0G4cWTslS4QUmfAAx7m8uUJGKak52BuQJVjN+iOTe2Q9m3nX8xw\n" +
//            "fhdVeg0U06hrRN+4gmlZ2Fd0XhVu7ZSLKYMPJbbkPfIDBYSMYVxX0KU+mOYz\n" +
//            "-----END RSA PRIVATE KEY-----\n";

    SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
            session.setConfig("StrictHostKeyChecking", "no");
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch sch = super.createDefaultJSch(fs);
            sch.addIdentity(private_key); //添加私钥文件
//            sch.addIdentity("C:\\Users\\XPENG\\.ssh\\id_rsa"); //添加私钥文件
//            sch.addIdentity("gitlab-key", private_key.getBytes(StandardCharsets.UTF_8), null, null); //添加私钥文件
            return sch;
        }
    };


    public Git cloneRepository(String gitUrl, String codePath, String commitId) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setTransportConfigCallback(transport -> {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                })
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setDirectory(new File(codePath))
                .setBranch(commitId)
                .call();
        // 切换到指定commitId
        checkoutBranch(git, commitId);
        return git;
    }

    private static Ref checkoutBranch(Git git, String branch) {
        try {
            return git.checkout()
                    .setName(branch)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isValidGitRepository(String codePath) {
        Path folder = Paths.get(codePath);
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            // If it has been at least initialized
            if (RepositoryCache.FileKey.isGitRepository(folder.toFile(), FS.DETECTED)) {
                // we are assuming that the clone worked at that time, caller should call hasAtLeastOneReference
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


}