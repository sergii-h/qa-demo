export default class MockFetch {
    constructor(payload = {
        itemsResponse: {
            status: 200,
            body: [{
                id: '12345',
                name: 'name',
                description: 'description',
                amount: '1',
            }]
        },
        itemResponse: {
            status: 200,
            body: {
                id: '12345',
                name: 'name',
                description: 'description',
                amount: '1',
            }
        }
    }) {
        this.payload = payload;
    }

    execute = async (url, data) => {
        switch(data.method) {
            case 'GET':
                if (url.includes('/item/')) {
                    return {
                        status: this.payload.itemResponse.status,
                        json: () => this.payload.itemResponse.body,
                    }
                } else if (url.endsWith('/item')) {
                    return {
                        status: this.payload.itemsResponse.status,
                        json: () => this.payload.itemsResponse.body,
                    }
                }
                break

            case 'POST':
                if (url.includes('item')) {
                    return {
                        status: this.payload.itemResponse.status,
                        json: () => this.payload.itemResponse.body,
                    };
                }
                break

            case 'PUT':
                if (url.includes('item')) {
                    return {
                        ok: true,
                        status: this.payload.itemResponse.status,
                        json: () => this.payload.itemResponse.body,
                    }
                }
                break

            case 'DELETE':
                if (url.includes('/item/')) {
                    return {
                        status: 200,
                    };
                }
                break

            default:
                throw new Error(`Undefined fetch mock for url: ${url} and method: ${data.method}`);
        }
    }
}
