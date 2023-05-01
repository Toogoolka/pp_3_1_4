package ru.kata.spring.boot_security.demo.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService, RoleService, UserDetailsService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleServiceImpl roleServiceImpl;
    private final UserRepository userRepository;
    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, RoleServiceImpl roleServiceImpl, UserRepository userRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleServiceImpl = roleServiceImpl;
        this.userRepository = userRepository;
    }



    @Override
    public User findOne(Long id) {
        Optional<User> foundUser = userRepository.findById(id);
        if (foundUser == null) {
            throw new UsernameNotFoundException(String.format("User with id: '%f' - not found", id));
        }
        return foundUser.get();
    }

    @Transactional
    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            // ADD ROLE FOR NEW USER HERE
        if (user.getRoles().size() == 0) {
            user.setDefaultRole(roleServiceImpl.findRoleById(2L));
        }
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void update(Long id, User updatedUser) {
        updatedUser.setId(id);
        User existingUser = findOne(id);
        Set<Role> existingRoles = existingUser.getRoles();
        Set<Role> updatedRoles = updatedUser.getRoles();
        updatedUser.setPassword(bCryptPasswordEncoder.encode(updatedUser.getPassword()));
        if(!existingRoles.containsAll(updatedRoles)) {
            existingRoles.addAll(updatedRoles);
        }
        updatedUser.setRoles(existingRoles);
        userRepository.save(updatedUser);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAllByUsername(String name) {
        return userRepository.findByUsernameContainsIgnoreCase(name);
    }

    @Override
    public User findByUsername(String name) throws UsernameNotFoundException{
        return userRepository.findByUsername(name).orElse(null );
    }
    @Override
    public Role findRoleByName(String name) {
        return roleServiceImpl.findRoleByName(name);
    }

    @Override
    public Role findRoleById(Long id) {
        return roleServiceImpl.findRoleById(id);
    }

    @Override
    public List<Role> findAll() {
        return roleServiceImpl.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User '%s' not found", username));
        }
        return user;
    }
}
