package br.com.duckstore.payment.service;

import br.com.duckstore.payment.config.PaypalPaymentIntent;
import br.com.duckstore.payment.config.PaypalPaymentMethod;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaypalService {

    private final APIContext apiContext;
    private final List<Payment> allPayments;

    public PaypalService(APIContext apiContext) {
        this.apiContext = apiContext;
        this.allPayments = new ArrayList<>();
    }

    public Payment createPayment(
            Double total,
            String currency,
            PaypalPaymentMethod method,
            PaypalPaymentIntent intent,
            String description,
            String cancelUrl,
            String successUrl) throws PayPalRESTException {
        Amount amount = new Amount();
        BigDecimal totalBD = new BigDecimal(Double.toString(total)).setScale(2, RoundingMode.HALF_UP);
        String formattedTotal = totalBD.toString();
        amount.setCurrency(currency);
        amount.setTotal(formattedTotal);

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method.toString());

        Payment payment = new Payment();
        payment.setIntent(intent.toString());
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);


        allPayments.add(payment);
        listAllPayments().addAll(allPayments);

        return payment.create(apiContext);
    }

    public List<Payment> listAllPayments() {
        return allPayments;
    }

    public Payment getPaymentDetails(String paymentId) throws PayPalRESTException {
        return Payment.get(apiContext, paymentId);
    }

}