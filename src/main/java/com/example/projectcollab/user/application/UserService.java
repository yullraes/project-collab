package com.example.projectcollab.user.application;

import com.example.projectcollab.user.application.dto.CreateUserRequest;
import com.example.projectcollab.user.application.dto.UserResponse;
import com.example.projectcollab.user.persistence.UserEntity;
import com.example.projectcollab.user.persistence.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(final CreateUserRequest request) {
        if (request == null) {
            throw new UserValidationException("user.input.required");
        }

        String name = UserInputNormalizer.name(request.name());
        String email = UserInputNormalizer.email(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }

        UserEntity created;
        try {
            created = userRepository.saveAndFlush(UserEntity.create(name, email));
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException();
        }
        return toResponse(created);
    }

    public UserResponse getUser(final long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return toResponse(user);
    }

    private UserResponse toResponse(final UserEntity user) {
        return new UserResponse(
                user.userId(),
                user.name(),
                user.email(),
                user.createdAt()
        );
    }
}
