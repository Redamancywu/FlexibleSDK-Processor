package com.example.spring.services

import com.example.spring.models.User
import com.flexiblesdk.processor.annotation.ServiceProvider
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Notification service interface
 */
interface NotificationService {
    fun sendWelcomeNotification(user: User)
    fun sendUpdateNotification(user: User)
    fun sendDeletionNotification(user: User)
    fun sendCustomNotification(user: User, message: String)
}

/**
 * Email notification service implementation
 * This is a Spring service that is also registered with FlexibleSDK
 */
@ServiceProvider(
    interfaces = [NotificationService::class],
    priority = 100,
    singleton = true
)
@Service
class EmailNotificationService : NotificationService {
    
    private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)
    
    override fun sendWelcomeNotification(user: User) {
        logger.info("Sending welcome email to ${user.email}")
        // Simulate email sending
        simulateEmailSending(
            to = user.email,
            subject = "Welcome to our platform!",
            body = "Hello ${user.name}, welcome to our platform!"
        )
    }
    
    override fun sendUpdateNotification(user: User) {
        logger.info("Sending update notification to ${user.email}")
        simulateEmailSending(
            to = user.email,
            subject = "Profile Updated",
            body = "Hello ${user.name}, your profile has been updated successfully."
        )
    }
    
    override fun sendDeletionNotification(user: User) {
        logger.info("Sending deletion notification to ${user.email}")
        simulateEmailSending(
            to = user.email,
            subject = "Account Deleted",
            body = "Hello ${user.name}, your account has been deleted. We're sorry to see you go!"
        )
    }
    
    override fun sendCustomNotification(user: User, message: String) {
        logger.info("Sending custom notification to ${user.email}: $message")
        simulateEmailSending(
            to = user.email,
            subject = "Notification",
            body = "Hello ${user.name}, $message"
        )
    }
    
    private fun simulateEmailSending(to: String, subject: String, body: String) {
        // Simulate email sending delay
        Thread.sleep(100)
        
        logger.debug("Email sent successfully:")
        logger.debug("  To: $to")
        logger.debug("  Subject: $subject")
        logger.debug("  Body: $body")
    }
}