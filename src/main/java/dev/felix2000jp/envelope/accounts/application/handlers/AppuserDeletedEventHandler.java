package dev.felix2000jp.envelope.accounts.application.handlers;

import dev.felix2000jp.envelope.accounts.application.AccountService;
import dev.felix2000jp.envelope.appusers.domain.events.AppuserDeletedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class AppuserDeletedEventHandler {

    private final AccountService accountService;

    AppuserDeletedEventHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @ApplicationModuleListener
    void on(AppuserDeletedEvent event) {
        accountService.deleteByUserId(event.appuserId());
    }

}
