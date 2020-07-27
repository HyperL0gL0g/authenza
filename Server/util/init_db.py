import psycopg2
import time
import os

def initDB():
    retries=0
    while retries<=5:
        try:
            conn = psycopg2.connect(
                f"dbname='{os.getenv('DB_NAME')}' user='{os.getenv('DB_USER')}' host='authenza_db' password='{os.getenv('DB_PASSWORD')}'")
            createTables(conn)
            return conn
        except Exception as e:
            print("\nXX ERROR XX :", e)
            print("\n!! RETRYING AFTER 12s !!")
            time.sleep(12)
            retries+=1
    print("\nXX CONNECTION FAILED XX")
    return None


def createTables(conn):
    try:
        # conn = init()
        cur = conn.cursor()

        print("\n!! CREATING organization TABLE")
        cur.execute("CREATE TABLE IF NOT EXISTS organization (\
												id varchar(60) PRIMARY KEY,\
												name varchar(100) NOT NULL,\
												email varchar(80) UNIQUE NOT NULL,\
												password varchar(60) NOT NULL,\
												image varchar(170) NOT NULL)")

        print("\n!! CREATING org_api TABLE")
        cur.execute("CREATE TABLE IF NOT EXISTS org_api (\
											org_id varchar(60) REFERENCES organization(id),\
											api_key varchar(60) PRIMARY KEY,\
											description TEXT,\
											name varchar(100) NOT NULL)")

        # print("\n!! CREATING org_session TABLE")
        # cur.execute("CREATE TABLE IF NOT EXISTS org_session(\
        # 									org_id varchar(60) REFERENCES organization(id),\
        # 									session_key varchar(60) PRIMARY KEY,\
        # 									issue_time TIMESTAMPTZ DEFAULT NOW())")

        print("\n!! CREATING users TABLE")
        cur.execute("CREATE TABLE IF NOT EXISTS users (\
										org_id varchar(60) REFERENCES organization(id),\
										id varchar(60) UNIQUE NOT NULL,\
										name varchar(100) NOT NULL,\
										email varchar(80),\
										password varchar(60) NOT NULL,\
										two_fact BOOL DEFAULT false,\
										verified BOOL DEFAULT false,\
										PRIMARY KEY (org_id,email))")

        # print("\n!! CREATING user_session TABLE")
        # cur.execute("CREATE TABLE IF NOT EXISTS user_session(\
        # 									org_id varchar(60) REFERENCES organization(id),\
        # 									u_id varchar(60) REFERENCES users(id),\
        # 									session_key varchar(60) PRIMARY KEY,\
        # 									issue_time TIMESTAMPTZ DEFAULT NOW(),\
        # 									complete BOOL DEFAULT false)")

        print("\n!! CREATING user_2_factor TABLE")
        cur.execute("CREATE TABLE IF NOT EXISTS user_2_factor(\
											org_id varchar(60) REFERENCES organization(id),\
											u_id varchar(60) REFERENCES users(id),\
											type varchar(5) NOT NULL,\
											qr varchar(60),\
											issue_time NUMERIC NOT NULL)")

        print("\n!! CREATING code_verify TABLE")
        cur.execute("CREATE TABLE IF NOT EXISTS code_verify(\
											org_id varchar(60) NOT NULL,\
											email varchar(60) NOT NULL,\
											code varchar(8) NOT NULL,\
											issue_time NUMERIC NOT NULL,\
											type varchar(6) NOT NULL,\
											PRIMARY KEY (org_id,code))")

        print("\n!! CREATING email_verify TABLE")
        cur.execute("CREATE TABLE IF NOT EXISTS email_verify(\
											org_id varchar(60) NOT NULL,\
											email varchar(60) NOT NULL,\
											code varchar(8) NOT NULL,\
											issue_time NUMERIC NOT NULL,\
											PRIMARY KEY (org_id,code))")

        cur.close()
        conn.commit()

    except Exception as e:
        print("\nXX ERROR XX :", e)


if __name__ == '__main__':
    print("\n!! MANUAL DB MIGRATION !!\n")
    conn=initDB()
    conn.close()
