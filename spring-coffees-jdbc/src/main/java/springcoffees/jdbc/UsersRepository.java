package springcoffees.jdbc;

import springcoffees.domain.User;

import java.util.List;
import java.util.Optional;

public interface UsersRepository {

    List<User> findByEnabledAndAuthorityOrderBy(User.Enabled enabled, User.Authority authority,
                                                User.OrderBy orderBy);

    List<User> findAllOrderBy(User.OrderBy orderBy);

    User findByUsername(String username);

    User save(User user);

    void updateNameAndEmailByUsername(String username, String firstName, String lastName, String email);

    void updateEnabledAndAuthorityByUsername(String username, User.Enabled enabled, User.Authority authority);

    void deleteByUsername(String username);

    Optional<String> getNewResetTokenFor(String email);

    Optional<User> updatePasswordByToken(String password, String token);

}
