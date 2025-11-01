package Backend.demo.Controllers;

import Backend.demo.Entities.UserInfo;
import Backend.demo.Repositories.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserInfoController {
    @Autowired
    private UserInfoRepository userInfoRepository;

    @GetMapping
    public List<UserInfo> getAllUsers() {
        return userInfoRepository.findAll();
    }

    @GetMapping("/{id}")
    public UserInfo getUserById(@PathVariable Integer id) {
        return userInfoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " not found"));
    }

    @PostMapping
    public UserInfo createUser(@RequestBody UserInfo user) {
        return userInfoRepository.save(user);
    }

    @PutMapping("/{id}")
    public UserInfo updateUser(@PathVariable Integer id, @RequestBody UserInfo updatedUser) {
        UserInfo existingUser = userInfoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " not found for update"));
        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setMail(updatedUser.getMail());
        return userInfoRepository.save(existingUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        if (!userInfoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " not found for deletion");
        }
        userInfoRepository.deleteById(id);
    }
}