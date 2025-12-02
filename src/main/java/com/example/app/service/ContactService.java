package com.example.app.service;

import com.example.app.entity.ContactMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    /**
     * お問い合わせ作成
     */
    @Transactional
    public ContactMessage createContact(ContactMessage message) {
        String sql = "INSERT INTO contact_message (name, email, subject, message, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, message.getName());
            ps.setString(2, message.getEmail());
            ps.setString(3, message.getSubject());
            ps.setString(4, message.getMessage());
            ps.setString(5, message.getStatus() != null ? message.getStatus() : "new");
            ps.setObject(6, LocalDateTime.now());
            return ps;
        }, keyHolder);
        
        Integer id = (Integer) keyHolder.getKeys().get("id");
        message.setId(id);
        
        return message;
    }

    /**
     * お問い合わせ一覧取得
     */
    public List<ContactMessage> getAllContacts() {
        String sql = "SELECT * FROM contact_message ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ContactMessage.class));
    }

    /**
     * お問い合わせ詳細取得
     */
    public ContactMessage getContactById(Integer id) {
        try {
            String sql = "SELECT * FROM contact_message WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ContactMessage.class), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * お問い合わせ更新
     */
    @Transactional
    public ContactMessage updateContact(ContactMessage message) {
        String sql = "UPDATE contact_message SET name = ?, email = ?, subject = ?, message = ?, status = ? " +
                     "WHERE id = ?";
        
        int rows = jdbcTemplate.update(sql,
                message.getName(),
                message.getEmail(),
                message.getSubject(),
                message.getMessage(),
                message.getStatus(),
                message.getId());
        
        if (rows > 0) {
            return getContactById(message.getId());
        }
        return null;
    }

    /**
     * お問い合わせに返信（メール送信 + DB更新）
     */
    @Transactional
    public ContactMessage replyToContact(Integer id, String replyText, String username) {
        ContactMessage contact = getContactById(id);
        if (contact == null) {
            throw new RuntimeException("お問い合わせが見つかりません");
        }

        // メール送信
        String emailSubject = "Re: " + contact.getSubject();
        emailService.sendEmail(contact.getEmail(), emailSubject, replyText);

        // DB更新
        String sql = "UPDATE contact_message SET reply = ?, replied_at = ?, replied_by = ?, status = ? " +
                     "WHERE id = ?";
        
        jdbcTemplate.update(sql,
                replyText,
                LocalDateTime.now(),
                username,
                "replied",
                id);

        return getContactById(id);
    }

    /**
     * お問い合わせ削除
     */
    @Transactional
    public boolean deleteContact(Integer id) {
        String sql = "DELETE FROM contact_message WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }
}
