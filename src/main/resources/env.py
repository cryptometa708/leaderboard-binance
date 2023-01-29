import win32com.client
import sys
import random
import string


def send_outlook_html_mail(subject='test subject',attachment='', body='test message', to='oli.boy@wanadoo.fr', recipients='oli.boy@wanadoo.fr', send_or_display='Display' , link='https://google.fr'):
    """
    Send an Outlook HTML email
    :param recipients: list of recipients' email addresses (list object)
    :param subject: subject of the email
    :param body: HTML body of the email
    :param send_or_display: Send - send email automatically | Display - email gets created user have to click Send
    :param copies: list of CCs' email addresses
    :return: None
    """
    if len(recipients) > 0:
        outlook = win32com.client.Dispatch("Outlook.Application").GetNameSpace("MAPI")
		
        ol_msg = outlook.OpenSharedItem(r"{}".format(body));
        ol_msg.HTMLBody = ol_msg.HTMLBody.replace("--link--" , link);
        ol_msg.To = to
        ol_msg.HTMLBody = ol_msg.HTMLBody.replace("--rand--" , ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(30)));
        if recipients != "-":
        	ol_msg.BCC = recipients
        ol_msg.Subject = subject
        
        if attachment != "-":
            ol_msg.Attachments.Add(attachment)
        if send_or_display.upper() == 'SEND':
            ol_msg.Send()
            print("success")
        else:
            ol_msg.Save()
            ol_msg.Display()
    else:
        print('Recipient email address - NOT FOUND')


if __name__ == '__main__':
	#print("Argument List:"+ str(sys.argv))
    #FROM_EMAIL = sys.argv[2]
    SUBJECT = sys.argv[1]
    ATTACHMENT = sys.argv[2]
    MAIL_BODY = sys.argv[3]
    TO = sys.argv[4]
    LIST_MAIL = sys.argv[5]
    LINK = sys.argv[6]
    send_outlook_html_mail(subject=SUBJECT,attachment=ATTACHMENT, body=MAIL_BODY,to=TO,recipients=LIST_MAIL , send_or_display='Send' , link=LINK)