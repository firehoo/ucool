package dao;

import dao.entity.UserDO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:czy88840616@gmail.com">czy</a>
 * @since 10-12-3 上午12:16
 */
public class UserDAOImpl implements UserDAO, InitializingBean {
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDO getPersonInfo(String hostName) {
        String sql = "select * from user where host_name=?";
        UserDO user = null;
        List perList = this.jdbcTemplate.queryForList(sql, new Object[]{hostName});
        if (perList.size() >= 1) {
            user = new UserDO();
            Map map = (Map) perList.get(0);
            user.setId(Long.valueOf(String.valueOf(map.get("id"))));
            user.setHostName((String) map.get("host_name"));
            user.setName((String) map.get("name"));
            user.setConfig((Integer) map.get("config"));
            user.setMappingPath((String) map.get("mapping_path"));
            user.setGuid((String) map.get("guid"));
        }
        return user;
    }

    @Override
    public boolean createNewUser(UserDO userDO) {
        String sql = "insert into user (host_name, name, config, guid) values (?,?,?,?)";
        try {
            if (this.jdbcTemplate.update(sql, new Object[]{userDO.getHostName(), userDO.getName(), userDO.getConfig(), userDO.getGuid()}) > 0) {
                UserDO newUser = getPersonInfo(userDO.getHostName());
                userDO.setId(newUser.getId());
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    @Override
    public boolean updateDir(Long userId, String newDir, String oldDir) {
        if (newDir.equals(oldDir)) {
            return true;
        }
        String sql = "update user set name=? where id=? and name=?";
        try {
            this.jdbcTemplate.update(sql, new Object[]{newDir, userId, oldDir});
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    @Override
    public boolean updateConfig(Long userId, int newConfig, int srcConfig) {
        if (newConfig == srcConfig) {
            //这里return true不知道会不会有什么问题
            return true;
        }
        try {
            if (jdbcTemplate.update("update user set config=? where id=? and config=?", new Object[]{newConfig, userId, srcConfig}) > 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public boolean updateMappingPath(Long userId, String mappingPath, String srcMappingPath) {
        if (mappingPath.equals("{\"mappings\":[]}")) {
            srcMappingPath = null;
        }
        if (mappingPath.equals(srcMappingPath)) {
            //这里return true不知道会不会有什么问题
            return true;
        }
        try {
            String sql = "update user set mapping_path=? where id=? and mapping_path=?";
            if (srcMappingPath == null) {
                sql = "update user set mapping_path=? where id=?";
                if (jdbcTemplate.update(sql, new Object[]{mappingPath, userId}) > 0) {
                    return true;
                }
            } else {
                if (jdbcTemplate.update(sql, new Object[]{mappingPath, userId, srcMappingPath}) > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    @Override
    public UserDO getPersonInfoByGUID(String guid) {
        String sql = "select * from user where guid=?";
        UserDO user = null;
        List perList = this.jdbcTemplate.queryForList(sql, new Object[]{guid});
        if (perList.size() == 1) {
            user = new UserDO();
            Map map = (Map) perList.get(0);
            user.setId(Long.valueOf(String.valueOf(map.get("id"))));
            user.setHostName((String) map.get("host_name"));
            user.setName((String) map.get("name"));
            user.setConfig((Integer) map.get("config"));
            user.setMappingPath((String) map.get("mapping_path"));
            user.setGuid((String) map.get("guid"));
        }
        return user;
    }

    @Override
    public boolean updateHostName(Long userId, String newHostName, String srcHostName) {
        if (newHostName.equals(srcHostName)) {
            return true;
        }
        try {
            String sql = "update user set host_name=? where id=? and host_name=?";
            if (jdbcTemplate.update(sql, new Object[]{newHostName, userId, srcHostName}) > 0) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    @Override
    public boolean updateGUID(Long userId, String guid, String oldguid) {
        if (guid.equals(oldguid)) {
            return true;
        }
        try {
            String sql = "update user set guid=? where id=? and guid=?";
            if (jdbcTemplate.update(sql, new Object[]{guid, userId, oldguid}) > 0) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int userExist = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM sqlite_master where type=\'table\' and name=?", new Object[]{"user"});
        //create table
        if (userExist == 0) {
            jdbcTemplate.execute("CREATE TABLE \"user\" (\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"host_name\" VARCHAR NOT NULL , \"name\" VARCHAR NOT NULL , \"config\" INTEGER NOT NULL  DEFAULT 5, \"mapping_path\" VARCHAR, \"guid\" VARCHAR)");
            jdbcTemplate.execute("CREATE  INDEX \"main\".\"idx_hostname\" ON \"user\" (\"host_name\" ASC)");
            jdbcTemplate.execute("CREATE  INDEX \"main\".\"idx_guid\" ON \"user\" (\"guid\" ASC)");
        }
    }
}
